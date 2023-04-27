package org.xyz.mysqlproxy.net.handler.backend;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.mysqlproxy.net.connection.BackendConnection;
import org.xyz.mysqlproxy.net.proto.mysql.BinaryPacketDef;

@Slf4j
public class BackendCommandHandler extends ChannelInboundHandlerAdapter  {
    protected BackendConnection backendConnection;

    public BackendCommandHandler(BackendConnection backendConnection) {
        this.backendConnection = backendConnection;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacketDef bin = (BinaryPacketDef) msg;
        log.debug(bin.toString());
        super.channelRead(ctx, msg);
    }

}

