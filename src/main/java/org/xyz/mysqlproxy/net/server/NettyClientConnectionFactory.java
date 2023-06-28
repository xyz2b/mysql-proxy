package org.xyz.mysqlproxy.net.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.xyz.mysqlproxy.config.ProxyConfig;
import org.xyz.mysqlproxy.net.handler.BackendInboundHandler;

import java.net.SocketAddress;

@RequiredArgsConstructor
public class NettyClientConnectionFactory {
    private final ProxyConfig proxyConfig;

    public ChannelFuture connect(final Channel frontChannel, final SocketAddress socketAddress) {
        final Bootstrap bootstrap = new Bootstrap()
                .group(frontChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new BackendInboundHandler(frontChannel))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(EpollChannelOption.SO_REUSEPORT, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .localAddress(proxyConfig.getIp(), 0)
                .remoteAddress(socketAddress);

        return bootstrap.connect();
    }

}
