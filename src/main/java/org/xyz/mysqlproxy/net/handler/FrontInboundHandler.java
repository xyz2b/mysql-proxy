package org.xyz.mysqlproxy.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
@Slf4j
public class FrontInboundHandler extends ChannelInboundHandlerAdapter {
    private final ProxyEndpoint proxyEndpoint;

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        proxyEndpoint.apply(msg, ctx);
        ctx.fireChannelRead(msg);
    }
}
