package org.xyz.mysqlproxy.net.handler.factory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.mysqlproxy.config.ProxyConfig;
import org.xyz.mysqlproxy.net.codec.MySqlPacketDecoder;
import org.xyz.mysqlproxy.net.connection.FrontConnectionFactory;
import org.xyz.mysqlproxy.net.connection.FrontendConnection;
import org.xyz.mysqlproxy.net.handler.FrontInboundHandler;
import org.xyz.mysqlproxy.net.handler.ProxyEndpoint;
import org.xyz.mysqlproxy.net.handler.exception.ServerExceptionHandler;
import org.xyz.mysqlproxy.net.handler.frontend.FrontendAuthenticator;
import org.xyz.mysqlproxy.net.server.NettyClientConnectionFactory;
import org.xyz.mysqlproxy.net.session.BasicNettyOrigin;
import org.xyz.mysqlproxy.net.session.NettyOrigin;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Service("FrontHandlerFactory")
public class FrontHandlerFactory extends ChannelInitializer<SocketChannel>  {
    @Autowired
    private ProxyConfig proxyConfig;

    private FrontConnectionFactory factory;

    public FrontHandlerFactory() {
        factory = new FrontConnectionFactory();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
//        FrontendConnection frontendConnection = factory.getConnection();
//
//        // 空闲连接检测handler，同mysql: wait_timeout参数
//        // ch.pipeline().addLast(new IdleStateHandler(10, 10, 10));
//        // decode mysql packet depend on it's length
//        ch.pipeline().addLast("mySqlPacketDecoder", new MySqlPacketDecoder());
//        ch.pipeline().addLast("frontendAuthenticator", new FrontendAuthenticator(frontendConnection, proxyConfig));
//        ch.pipeline().addLast("serverException", new ServerExceptionHandler());

        NettyClientConnectionFactory nettyClientConnectionFactory = new NettyClientConnectionFactory(proxyConfig);
        SocketAddress socketAddress = new InetSocketAddress(proxyConfig.getBackendIp(), proxyConfig.getBackendPort());
        NettyOrigin nettyOrigin = new BasicNettyOrigin(nettyClientConnectionFactory, socketAddress);
        ProxyEndpoint proxyEndpoint = new ProxyEndpoint(nettyOrigin);
        FrontInboundHandler frontInboundHandler = new FrontInboundHandler(proxyEndpoint);
        ch.pipeline().addLast("FrontInboundHandler", frontInboundHandler);
        ch.pipeline().addLast("serverException", new ServerExceptionHandler());
    }
}
