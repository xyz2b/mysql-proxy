package org.xyz.dbproxy.proxy.frontend;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.xyz.dbproxy.infra.config.props.ConfigurationPropertyKey;
import org.xyz.dbproxy.proxy.backend.context.BackendExecutorContext;
import org.xyz.dbproxy.proxy.backend.context.ProxyContext;
import org.xyz.dbproxy.proxy.frontend.netty.ServerHandlerInitializer;
import org.xyz.dbproxy.proxy.frontend.protocol.FrontDatabaseProtocolTypeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * DbProxy.
 */
@Slf4j
public final class DbProxy {
    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workerGroup;

    public DbProxy() {
        bossGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        workerGroup = getWorkerGroup();
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    private EventLoopGroup getWorkerGroup() {
        int workerThreads = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE);
        return Epoll.isAvailable() ? new EpollEventLoopGroup(workerThreads) : new NioEventLoopGroup(workerThreads);
    }

    /**
     * Start DbProxy.
     *
     * @param port      port
     * @param addresses addresses
     */
    @SneakyThrows(InterruptedException.class)
    public void start(final int port, final List<String> addresses) {
        try {
            List<ChannelFuture> futures = startInternal(port, addresses);
            accept(futures);
        } finally {
            close();
        }
    }

    /**
     * Start DbProxy with DomainSocket.
     * DomainSocket的全称是unix domain socket，它又可以叫做IPC socket,也就是inter-process communication socket，是在unix平台上的同一服务器上的进程间通信方式。
     * 协议是比较复杂的，对于传统的socket通讯来说,需要定制特定的协议，然后进行封包和解包等操作，但是使用DomainSocket，可以直接将进程的数据直接拷贝，从而节约了时间，并提高了程序的效率。
     * 在ServerDomainSocketChannel中的remoteAddress和localAddress的类型都是DomainSocketAddress,DomainSocketAddress有一个socketPath属性，用来存储DomainSocket文件的路径。
     * @param socketPath socket path
     */
    public void start(final String socketPath) {
        if (!Epoll.isAvailable()) {
            log.error("Epoll is unavailable, DomainSocket can't start.");
            return;
        }
        ChannelFuture future = startDomainSocket(socketPath);
        future.addListener((ChannelFutureListener) futureParams -> {
            if (futureParams.isSuccess()) {
                log.info("The listening address for DomainSocket is {}", socketPath);
            } else {
                log.error("DomainSocket failed to start:{}", futureParams.cause().getMessage());
            }
        });
    }

    private List<ChannelFuture> startInternal(final int port, final List<String> addresses) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        initServerBootstrap(bootstrap);
        List<ChannelFuture> futures = new ArrayList<>();
        for (String address : addresses) {
            futures.add(bootstrap.bind(address, port).sync());
        }
        return futures;
    }

    private ChannelFuture startDomainSocket(final String socketPath) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        initServerBootstrap(bootstrap, new DomainSocketAddress(socketPath));
        return bootstrap.bind();
    }

    private void accept(final List<ChannelFuture> futures) throws InterruptedException {
        log.info("ShardingSphere-DbProxy {} mode started successfully", ProxyContext.getInstance().getContextManager().getInstanceContext().getModeConfiguration().getType());
        for (ChannelFuture future : futures) {
            future.channel().closeFuture().sync();
        }
    }

    private void initServerBootstrap(final ServerBootstrap bootstrap) {
        Integer backLog = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG);
        bootstrap.group(bossGroup, workerGroup)
                .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(8 * 1024 * 1024, 16 * 1024 * 1024))
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, backLog)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerHandlerInitializer(FrontDatabaseProtocolTypeFactory.getDatabaseType()));
    }

    private void initServerBootstrap(final ServerBootstrap bootstrap, final DomainSocketAddress localDomainSocketAddress) {
        bootstrap.group(bossGroup, workerGroup)
                .channel(EpollServerDomainSocketChannel.class)
                .localAddress(localDomainSocketAddress)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerHandlerInitializer(FrontDatabaseProtocolTypeFactory.getDatabaseType()));
    }

    private void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        BackendExecutorContext.getInstance().getExecutorEngine().close();
    }
}