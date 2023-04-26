package org.xyz.proxy.net.handler.frontend;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.proxy.config.ProxyConfig;
import org.xyz.proxy.net.connection.FrontendConnection;
import org.xyz.proxy.net.constants.*;
import org.xyz.proxy.net.proto.mysql.*;
import org.xyz.proxy.net.util.SecurityUtil;
import org.xyz.proxy.util.RandomUtil;

import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;

// 前端通道Active处理器
@Slf4j
public class FrontendAuthenticator extends ChannelInboundHandlerAdapter {
    // 盐值
    private byte[] seed;

    private FrontendConnection frontendConnection;

    private ProxyConfig proxyConfig;

    public FrontendAuthenticator(FrontendConnection frontendConnection, ProxyConfig proxyConfig) {
        this.frontendConnection = frontendConnection;
        this.proxyConfig = proxyConfig;
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
        HandshakePacketDef hs = new HandshakePacketDef();
        hs.setSequenceId((byte) 0);
        hs.setProtocolVersion(Versions.PROTOCOL_VERSION);
        hs.setServerVersion(Versions.SERVER_VERSION);
        hs.setThreadId(11);
        hs.setSeed(seed);
        hs.setServerCapabilities(frontendConnection.getServerCapabilities());
        hs.setCharacterSetIndex(CharacterSet.getIndex("utf8mb4_general_ci"));
        hs.setServerStatus(StatusFlags.SERVER_STATUS_AUTOCOMMIT);
        hs.setAuthPluginName(proxyConfig.getDefaultAuthPlugin());
        hs.write(ctx);
    }

    // 接收 握手响应，认证用户和密码
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacketDef bin = (BinaryPacketDef) msg;
        HandshakeResponsePacketDef hsp = new HandshakeResponsePacketDef();
        hsp.read(bin);

        frontendConnection.setClientCapabilities(hsp.getClientFlag());
        frontendConnection.setUser(hsp.getUserName());
        frontendConnection.setDatabase(hsp.getDatabase());

        if(!hsp.getClientPluginName().equals(proxyConfig.getDefaultAuthPlugin())) {  // 服务端使用的加密算法是使用和client auth plugin不同的算法进行加密的，需要进行 auth switch，服务端发送 auth switch request
            ctx.pipeline().replace(this, "FrontendAuthSwitchHandler", new FrontendAuthSwitchHandler(frontendConnection, proxyConfig));
            return;
        }
        // check password
        if (!checkPassword(hsp.getPassword(), hsp.getUserName())) {
            String host =  ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
            String errorMsg = String.format("Access denied for user '%s'@'%s' (using password: YES)", frontendConnection.getUser(), host);
            failure(ctx, ErrorCode.ER_ACCESS_DENIED_ERROR, errorMsg);
            ctx.close();
            return;
        }
        success(ctx);
    }

    private void success(final ChannelHandlerContext ctx) {
        // 认证通过，将frontendConnection和front channel互相绑定
        frontendConnection.setFrontChannel(ctx.channel());
        ctx.channel().attr(FrontendConnection.FRONTEND_CONNECTION).set(frontendConnection);

        // AUTH_OK , process command
        ctx.pipeline().replace(this, "frontCommandHandler", new FrontendCommandHandler(frontendConnection));
        // 发送 AUTH_OK 响应报文
        ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(OkPacketDef.AUTH_OK_PACKET);
        // just io , no need thread pool
        ctx.writeAndFlush(byteBuf);
    }

    protected boolean checkPassword(byte[] password, String user) throws NoSuchAlgorithmException {
        // todo config
        String pass = "test";
        // 存储在mysql.user表的authentication_string字段的值 = SHA1(SHA1( pass ))
        byte[] p = SecurityUtil.scrambleSha1Sha1(pass.getBytes());

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
            encryptPass = SecurityUtil.decodeClientPassword(password, seed, p);
        } catch (NoSuchAlgorithmException e) {
            log.warn("", e);
            return false;
        }
        if (encryptPass != null && (encryptPass.length == p.length)) {
            int i = encryptPass.length;
            while (i-- != 0) {
                if (encryptPass[i] != p[i]) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private void failure(final ChannelHandlerContext ctx, int errno, String info) {
        log.error(info);
        // 发送ERROR报文
        ErrorPacketDef errorPacket = new ErrorPacketDef(frontendConnection.getServerCapabilities());
        errorPacket.setSequenceId((byte) 4);
        errorPacket.setErrorCode(errno);
        errorPacket.setSqlStateMarker("#");
        errorPacket.setSqlState("28000");
        errorPacket.setErrorMessage(info);
        errorPacket.write(ctx);
    }
}
