package org.xyz.mysqlproxy.net.handler.factory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.stereotype.Service;
import org.xyz.mysqlproxy.net.codec.MySqlPacketDecoder;
import org.xyz.mysqlproxy.net.connection.BackendConnection;
import org.xyz.mysqlproxy.net.connection.BackendConnectionFactory;
import org.xyz.mysqlproxy.net.handler.backend.BackendAuthenticator;
import org.xyz.mysqlproxy.net.handler.exception.ServerExceptionHandler;

@Service("BackendHandlerFactory")
public class BackendHandlerFactory extends ChannelInitializer<SocketChannel>  {
    private BackendConnectionFactory factory;

    public BackendHandlerFactory() {
        factory = new BackendConnectionFactory();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        BackendConnection backendConnection = factory.getConnection();

        // 空闲连接检测handler，同mysql: wait_timeout参数
        // ch.pipeline().addLast(new IdleStateHandler(10, 10, 10));
        // decode mysql packet depend on it's length
        ch.pipeline().addLast("mySqlPacketDecoder", new MySqlPacketDecoder());
        ch.pipeline().addLast("backendAuthenticator", new BackendAuthenticator(backendConnection));
        ch.pipeline().addLast("serverException", new ServerExceptionHandler());
    }
}
