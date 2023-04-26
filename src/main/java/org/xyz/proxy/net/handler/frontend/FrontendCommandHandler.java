package org.xyz.proxy.net.handler.frontend;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.proxy.net.connection.FrontendConnection;
import org.xyz.proxy.net.proto.mysql.BinaryPacketDef;

@Slf4j
public class FrontendCommandHandler extends ChannelInboundHandlerAdapter {
    protected FrontendConnection frontendConnection;

    public FrontendCommandHandler(FrontendConnection frontendConnection) {
        this.frontendConnection = frontendConnection;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BinaryPacketDef bin = (BinaryPacketDef) msg;
        log.debug(bin.toString());
        super.channelRead(ctx, msg);
    }
}
