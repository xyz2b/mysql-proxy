package org.xyz.proxy.net.handler.backend;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.proxy.net.exception.ErrorPacketException;
import org.xyz.proxy.net.exception.UnknownPacketException;
import org.xyz.proxy.net.proto.mysql.*;
import org.xyz.proxy.net.proto.util.SecurityUtil;
import org.xyz.proxy.net.constants.BackendConnState;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.constants.CharacterSet;

import java.security.NoSuchAlgorithmException;

// 后端连接处理器
@Slf4j
public class BackendAuthenticator extends ChannelInboundHandlerAdapter {
    private int state = BackendConnState.BACKEND_HANDSHAKE_RESPONSE_NOT_SEND;

    private static final long CLIENT_FLAGS = getClientFlags();
    private static final long MAX_PACKET_SIZE = 1024 * 1024 * 16;

    public BackendAuthenticator() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        switch (state) {
            case (BackendConnState.BACKEND_HANDSHAKE_RESPONSE_NOT_SEND):
                // 处理握手包并发送auth包
                auth(ctx, msg);
                // 修改连接状态
                state = BackendConnState.BACKEND_HANDSHAKE_RESPONSE_SEND;
                break;
            case (BackendConnState.BACKEND_HANDSHAKE_RESPONSE_SEND):
                authOk(ctx, msg);
                break;
            default:
                break;
        }
    }

    private void authOk(ChannelHandlerContext ctx, Object msg) {
        BinaryPacket bin = (BinaryPacket) msg;
        int responsePacketId = bin.getPayload()[0];
        switch (responsePacketId) {
            case OkPacket.PACKET_ID:
                afterSuccess();
                break;
            case ErrorPacket.PACKET_ID:
                ErrorPacket err = new ErrorPacket();
                err.read(bin);
                throw new ErrorPacketException("Auth not Okay");
            default:
                throw new UnknownPacketException("Unknown packet id: " + responsePacketId);
        }

        // replace the commandHandler of Authenticator
        ctx.pipeline().replace(this, "BackendCommandHandler", new BackendCommandHandler());
    }

    /**
     * 连接和验证成功以后
     */
    private void afterSuccess() {
        // todo
        //  if (dsc.getSqlMode() != null) {
        //      sendSqlMode();
        //  }
        // 为防止握手阶段字符集编码交互无效，连接成功之后做一次字符集编码同步。
        // sendCharset(charsetIndex);
        log.info("auth okay");
    }

    private void auth(ChannelHandlerContext ctx, Object msg) {
        HandshakePacket hsp = new HandshakePacket();
        hsp.read((BinaryPacket) msg);
        try {
            auth(hsp, ctx);
        } catch (NoSuchAlgorithmException e) {
            ctx.close();
            log.error("create auth packet error: ", e);
        }
    }

    private void auth(HandshakePacket hsp, ChannelHandlerContext ctx)
            throws NoSuchAlgorithmException {
        HandshakeResponsePacket hp = new HandshakeResponsePacket();
        hp.setSequenceId(1);
        hp.setClientFlag(CLIENT_FLAGS);
        hp.setMaxPacketSize(MAX_PACKET_SIZE);
        hp.setCharsetIndex(CharacterSet.getIndex("utf8mb4_general_ci"));
        // todo config
        String username = "test";
        hp.setUserName(username);
        String passwd = "test1";
        if (passwd != null && passwd.length() > 0) {
            byte[] password = passwd.getBytes();
            byte[] seed = hsp.getSeed();
            hp.setPassword(SecurityUtil.scramble411(password, seed));
        }
        // todo config
        String database = "test";
        hp.setDatabase(database);
        hp.write(ctx);
    }

    /**
     * 与MySQL连接时的一些特性指定
     */
    private static long getClientFlags() {
        int flag = 0;
        flag |= CapabilitiesFlags.CLIENT_LONG_PASSWORD;
        flag |= CapabilitiesFlags.CLIENT_FOUND_ROWS;
        flag |= CapabilitiesFlags.CLIENT_LONG_FLAG;
        flag |= CapabilitiesFlags.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= CapabilitiesFlags.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= CapabilitiesFlags.CLIENT_IGNORE_SPACE;
        flag |= CapabilitiesFlags.CLIENT_PROTOCOL_41;
        flag |= CapabilitiesFlags.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= CapabilitiesFlags.CLIENT_IGNORE_SIGPIPE;
        flag |= CapabilitiesFlags.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        // client extension
        // 不允许MULTI协议
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }
}