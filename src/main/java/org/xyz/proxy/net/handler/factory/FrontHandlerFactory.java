package org.xyz.proxy.net.handler.factory;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.proxy.net.codec.MySqlPacketDecoder;
import org.xyz.proxy.net.handler.exception.ServerExceptionHandler;

@Service("FrontHandlerFactory")
public class FrontHandlerFactory extends ChannelInitializer<SocketChannel>  {
    @Autowired
    private ServerExceptionHandler serverExceptionHandler;

    @Autowired
    private MySqlPacketDecoder mySqlPacketDecoder;

    public FrontHandlerFactory() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 空闲连接检测handler，同mysql: wait_timeout参数
        // ch.pipeline().addLast(new IdleStateHandler(10, 10, 10));
        // decode mysql packet depend on it's length
        ch.pipeline().addLast("MySqlPacketDecoder", mySqlPacketDecoder);
        ch.pipeline().addLast("serverException", serverExceptionHandler);
    }
}
