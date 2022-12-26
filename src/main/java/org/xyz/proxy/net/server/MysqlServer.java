package org.xyz.proxy.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.proxy.config.NodeConfig;
import org.xyz.proxy.net.handler.factory.FrontHandlerFactory;

import java.net.InetSocketAddress;

@Data
@Slf4j
@Service("MysqlServer")
public class MysqlServer {
    @Autowired
    private NodeConfig nodeConfig;

    @Autowired
    private FrontHandlerFactory frontHandlerFactory;

    // 通过nio方式来接收连接和处理连接
    private EventLoopGroup bg;
    private EventLoopGroup wg;

    // 启动引导器
    private ServerBootstrap b = new ServerBootstrap();

    public void run() {
        String ip = nodeConfig.getIp();
        int port = nodeConfig.getPort();

        //连接监听线程组
        bg = new NioEventLoopGroup(1);
        //传输处理线程组
        wg = new NioEventLoopGroup();
        //1 设置reactor 线程
        b.group(bg, wg);
        //2 设置nio类型的channel
        b.channel(NioServerSocketChannel.class);
        //3 设置监听端口
        b.localAddress(new InetSocketAddress(ip, port));
        //4 设置通道选项
        b.option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(EpollChannelOption.SO_REUSEPORT, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(NioChannelOption.SO_KEEPALIVE, true);

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
        }

//        boolean isStart = false;
//        while (!isStart) {
//            try {
//                channelFuture = b.bind().sync();
//                log.info("Server 启动, 端口为：{}", channelFuture.channel().localAddress());
//                isStart = true;
//            } catch (Exception e) {
//                log.error("Server 启动失败端口为：{} msg: {}, 10s后重新启动", ip + ":" + port , e.getMessage());
//                log.error("Exception: ", e);
//                try {
//                    Thread.sleep(10000);
//                }  catch (InterruptedException t) {
//                    log.error("Exception: ", t);
//                }
//            }
//        }

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
                }));
        try {
            // 7 监听通道关闭事件
            // 应用程序会一直等待，直到channel关闭
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } catch (Exception e) {
            log.error("发生其他异常", e);
        } finally {
            // 8 优雅关闭EventLoopGroup，
            // 释放掉所有资源包括创建的线程
            wg.shutdownGracefully();
            bg.shutdownGracefully();
        }

    }
}
