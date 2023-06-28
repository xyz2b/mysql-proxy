package org.xyz.mysqlproxy.net.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BackendInboundHandler extends ChannelInboundHandlerAdapter {
    private final Channel frontChanel;

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        frontChanel.write(msg);
        frontChanel.flush();

        ctx.fireChannelRead(msg);
    }
}
