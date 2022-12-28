package org.xyz.proxy.net.handler.frontend;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.proxy.config.ProxyConfig;
import org.xyz.proxy.net.connection.FrontendConnection;
import org.xyz.proxy.net.constants.ErrorCode;
import org.xyz.proxy.net.constants.SessionStateTypes;
import org.xyz.proxy.net.constants.StatusFlags;
import org.xyz.proxy.net.proto.mysql.*;
import org.xyz.proxy.net.proto.util.SecurityUtil;
import org.xyz.proxy.util.RandomUtil;

import java.net.InetSocketAddress;
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
            String host =  ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();
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
        // 发送 OK 响应报文
        OkPacket ok = new OkPacket(frontendConnection.getServerCapabilities());
        ok.setSequenceId((byte) 4);
        ok.setAffectedRows(0L);
        ok.setLastInsertId(0);
        ok.setStatusFlags(StatusFlags.SERVER_STATUS_AUTOCOMMIT | StatusFlags.SERVER_SESSION_STATE_CHANGED);
        ok.setWarningCount(0);
        ok.setInfo("");

        String database = frontendConnection.getDatabase();
        ok.setType(SessionStateTypes.SESSION_TRACK_SCHEMA.getValue());   // SESSION_TRACK_SCHEMA name of the changed schema
        OkPacket.SessionTrackSchema sessionTrackSchema = new OkPacket.SessionTrackSchema();
        sessionTrackSchema.setType(SessionStateTypes.SESSION_TRACK_SCHEMA.getValue());
        sessionTrackSchema.setMandatoryFlag(5);
        sessionTrackSchema.setName(database);
        ok.setSessionStateInformation(sessionTrackSchema);
        ok.write(ctx);
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

    private void failure(final ChannelHandlerContext ctx, int errno, String info) {
        log.error(info);
        // 发送ERROR报文
        ErrorPacket errorPacket = new ErrorPacket(frontendConnection.getServerCapabilities());
        errorPacket.setSequenceId((byte) 4);
        errorPacket.setErrorCode(errno);
        errorPacket.setSqlStateMarker("#");
        errorPacket.setSqlState("28000");
        errorPacket.setErrorMessage(info);
        errorPacket.write(ctx);
    }

    public static void main(String[] args) {
        System.out.println("".getBytes().length);
    }
}
