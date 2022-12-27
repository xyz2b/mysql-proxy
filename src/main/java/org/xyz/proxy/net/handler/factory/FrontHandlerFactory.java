package org.xyz.proxy.net.handler.factory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.proxy.config.ProxyConfig;
import org.xyz.proxy.net.codec.MySqlPacketDecoder;
import org.xyz.proxy.net.connection.FrontConnectionFactory;
import org.xyz.proxy.net.connection.FrontendConnection;
import org.xyz.proxy.net.handler.exception.ServerExceptionHandler;
import org.xyz.proxy.net.handler.frontend.FrontendAuthenticator;

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
        FrontendConnection frontendConnection = factory.getConnection();

        // 空闲连接检测handler，同mysql: wait_timeout参数
        // ch.pipeline().addLast(new IdleStateHandler(10, 10, 10));
        // decode mysql packet depend on it's length
        ch.pipeline().addLast("mySqlPacketDecoder", new MySqlPacketDecoder());
        ch.pipeline().addLast("frontendAuthenticator", new FrontendAuthenticator(frontendConnection, proxyConfig));
        ch.pipeline().addLast("serverException", new ServerExceptionHandler());
    }
}
