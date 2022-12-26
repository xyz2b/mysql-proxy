package org.xyz.proxy.net.handler.factory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.proxy.net.codec.MySqlPacketDecoder;
import org.xyz.proxy.net.handler.backend.BackendAuthenticator;
import org.xyz.proxy.net.handler.exception.ServerExceptionHandler;

@Service("BackendHandlerFactory")
public class BackendHandlerFactory extends ChannelInitializer<SocketChannel>  {
    @Autowired
    private ServerExceptionHandler serverExceptionHandler;

    @Autowired
    private MySqlPacketDecoder mySqlPacketDecoder;

    @Autowired
    private BackendAuthenticator backendAuthenticator;

    public BackendHandlerFactory() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 空闲连接检测handler，同mysql: wait_timeout参数
        // ch.pipeline().addLast(new IdleStateHandler(10, 10, 10));
        // decode mysql packet depend on it's length
        ch.pipeline().addLast("mySqlPacketDecoder", mySqlPacketDecoder);
        ch.pipeline().addLast("backendAuthenticator", backendAuthenticator);
        ch.pipeline().addLast("serverException", serverExceptionHandler);
    }
}
