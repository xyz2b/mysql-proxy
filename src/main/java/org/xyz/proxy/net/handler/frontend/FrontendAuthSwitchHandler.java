package org.xyz.proxy.net.handler.frontend;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.proxy.config.ProxyConfig;
import org.xyz.proxy.net.connection.FrontendConnection;
import org.xyz.proxy.net.constants.ErrorCode;
import org.xyz.proxy.net.proto.mysql.*;
import org.xyz.proxy.net.proto.util.SecurityUtil;
import org.xyz.proxy.util.RandomUtil;

import java.security.NoSuchAlgorithmException;

@Slf4j
public class FrontendAuthSwitchHandler extends ChannelInboundHandlerAdapter {
    private byte[] seed;

    private FrontendConnection frontendConnection;

    private ProxyConfig proxyConfig;

    public FrontendAuthSwitchHandler(FrontendConnection frontendConnection, ProxyConfig proxyConfig ) {
        this.frontendConnection = frontendConnection;
        this.proxyConfig = proxyConfig;
    }

    // FrontendAuthSwitchHandler 该handler被添加，说明发生了 auth switch，服务端立即发送 AuthSwitchRequest
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        seed = RandomUtil.randomBytes(20);

        AuthSwitchRequest as = new AuthSwitchRequest();
        as.setSequenceId((byte) 2);
        as.setPluginName(proxyConfig.getDefaultAuthPlugin());
        as.setSeed(seed);
        as.write(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacket bin = (BinaryPacket) msg;
        AuthSwitchResponse asp = new AuthSwitchResponse();
        asp.read(bin);

        // check password
        if (!checkPassword(asp.getPassword(), frontendConnection.getUser())) {
            failure(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + frontendConnection.getUser() + "'");
            ctx.close();
            return;
        }
        success(ctx);
    }

    private void success(final ChannelHandlerContext ctx) {
        // AUTH_OK , process command
        ctx.pipeline().replace(this, "frontCommandHandler", new FrontendCommandHandler(frontendConnection));
        // AUTH_OK is stable
        ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(OkPacket.AUTH_OK_PACKET);
        // just io , no need thread pool
        ctx.writeAndFlush(byteBuf);
    }

    protected boolean checkPassword(byte[] password, String user) {
        // todo config
        String pass = "test";

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
