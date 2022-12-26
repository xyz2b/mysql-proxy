package org.xyz.proxy.net.handler.frontend;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xyz.proxy.net.connection.FrontendConnection;
import org.xyz.proxy.net.constants.*;
import org.xyz.proxy.net.proto.mysql.BinaryPacket;
import org.xyz.proxy.net.proto.mysql.HandshakePacket;
import org.xyz.proxy.net.proto.mysql.HandshakeResponsePacket;
import org.xyz.proxy.net.proto.mysql.OkPacket;
import org.xyz.proxy.net.proto.util.SecurityUtil;
import org.xyz.proxy.util.RandomUtil;
import java.security.NoSuchAlgorithmException;

// 前端通道Active处理器
@Slf4j
public class FrontendAuthenticator extends ChannelInboundHandlerAdapter {
    // 盐值
    private byte[] seed;

    private FrontendConnection frontendConnection;

    public FrontendAuthenticator(FrontendConnection frontendConnection) {
        this.frontendConnection = frontendConnection;
    }

    // 发送 握手初始化请求
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 生成认证数据
        byte[] rand1 = RandomUtil.randomBytes(8);
        byte[] rand2 = RandomUtil.randomBytes(12);

        // 保存认证数据
        byte[] seed = new byte[rand1.length + rand2.length];
        System.arraycopy(rand1, 0, seed, 0, rand1.length);
        System.arraycopy(rand2, 0, seed, rand1.length, rand2.length);
        this.seed = seed;

        // 握手初始化请求包
        HandshakePacket hs = new HandshakePacket();
        hs.setSequenceId((byte) 0);
        hs.setProtocolVersion(Versions.PROTOCOL_VERSION);
        hs.setServerVersion(Versions.SERVER_VERSION);
        hs.setThreadId(11);
        hs.setSeed(seed);
        hs.setServerCapabilities(frontendConnection.getServerCapabilities());
        hs.setCharacterSetIndex(CharacterSet.getIndex("utf8mb4_general_ci"));
        hs.setServerStatus(StatusFlags.SERVER_STATUS_AUTOCOMMIT);
        hs.setAuthPluginName(AuthPluginName.MYSQL_NATIVE_PASSWORD);
        hs.write(ctx);
    }

    // 接收 握手响应，认证用户和密码
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacket bin = (BinaryPacket) msg;
        HandshakeResponsePacket hsp = new HandshakeResponsePacket();
        hsp.read(bin);
        // check password
        if (!checkPassword(hsp.getPassword(), hsp.getUserName())) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + hsp.getUserName() + "'");
            ctx.close();
            return;
        }
        frontendConnection.setClientCapabilities(hsp.getClientFlag());
        success(ctx);
    }

    private void success(final ChannelHandlerContext ctx) {
        // AUTH_OK , process command
        ctx.pipeline().replace(this, "frontCommandHandler", new FrontendCommandHandler());
        // AUTH_OK is stable
        ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(OkPacket.AUTH_OK);
        // just io , no need thread pool
        ctx.writeAndFlush(byteBuf);
    }

    protected boolean checkPassword(byte[] password, String user) {
        // todo config
        String pass = "test1";

        // check null
        if (pass == null || pass.length() == 0) {
            if (password == null || password.length == 0) {
                return true;
            } else {
                return false;
            }
        }
        if (password == null || password.length == 0) {
            return false;
        }

        // encrypt
        byte[] encryptPass = null;
        try {
            encryptPass = SecurityUtil.scramble411(pass.getBytes(), seed);
        } catch (NoSuchAlgorithmException e) {
            log.warn("", e);
            return false;
        }
        if (encryptPass != null && (encryptPass.length == password.length)) {
            int i = encryptPass.length;
            while (i-- != 0) {
                if (encryptPass[i] != password[i]) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    protected void failure(int errno, String info) {
        log.error(info);
    }


}
