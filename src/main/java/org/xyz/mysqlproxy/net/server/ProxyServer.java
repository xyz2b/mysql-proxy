package org.xyz.mysqlproxy.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.mysqlproxy.config.ProxyConfig;
import org.xyz.mysqlproxy.net.handler.factory.FrontHandlerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Data
@Slf4j
@Service("ProxyServer")
public class ProxyServer {
    @Autowired
    private ProxyConfig proxyConfig;

    @Autowired
    private FrontHandlerFactory frontHandlerFactory;

    // 通过nio方式来接收连接和处理连接
    private EventLoopGroup bg;
    private EventLoopGroup wg;

    // 启动引导器
    private ServerBootstrap b = new ServerBootstrap();

    public void run() throws InterruptedException {
        String ip = proxyConfig.getIp();
        int port = proxyConfig.getPort();

        //连接监听线程组
        bg = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        //传输处理线程组
        wg = getWorkerGroup();
        //1 设置reactor 线程
        b.group(bg, wg);
        //2 设置nio类型的channel
        b.channel(NioServerSocketChannel.class);
        //3 设置监听端口
        b.localAddress(new InetSocketAddress(ip, port));
        //4 设置通道选项
        b.channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024 * 1024, 16 * 1024 * 1024))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO));

        //5 装配流水线
        b.childHandler(frontHandlerFactory);

        // 6 开始绑定server
        // 通过调用sync同步方法阻塞直到绑定成功
        ChannelFuture channelFuture = null;
        try {
            channelFuture = b.bind().sync();
            log.info("Server 启动, 端口为：{}", channelFuture.channel().localAddress());
        } catch (Exception e) {
            log.error("Server 启动失败，端口为：{} msg: {}", ip + ":" + port, e.getMessage());
            log.error("Exception: ", e);
            throw e;
        }

        // JVM关闭时的钩子函数
        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 8 优雅关闭EventLoopGroup，
                        // 释放掉所有资源包括创建的线程
                        wg.shutdownGracefully();
                        bg.shutdownGracefully();
                    }
                })
        );
        try {
            // 7 监听通道关闭事件
            // 应用程序会一直等待，直到channel关闭
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            log.error("发生其他异常", e);
            throw e;
        } finally {
            // 8 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            wg.shutdownGracefully();
            bg.shutdownGracefully();
        }
    }

    private EventLoopGroup getWorkerGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }
}
