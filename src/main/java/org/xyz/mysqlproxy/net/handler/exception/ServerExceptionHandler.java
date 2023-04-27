package org.xyz.mysqlproxy.net.handler.exception;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xyz.mysqlproxy.net.exception.InvalidFrameException;
import org.xyz.mysqlproxy.net.exception.InvalidMsgTypeException;

@Slf4j
@ChannelHandler.Sharable
@Service("ServerExceptionHandler")
public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof InvalidFrameException) {
            log.error(cause.getMessage());
        } else if (cause instanceof InvalidMsgTypeException) {
            log.error(cause.getMessage());
        } else {
            //捕捉异常信息
            log.error("Throwable: ", cause);
        }
        ctx.close();
    }

    /**
     * 通道 Read 读取 Complete 完成
     * 做刷新操作 ctx.flush()
     */
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
