package org.xyz.mysqlproxy.net.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import org.xyz.mysqlproxy.net.session.NettyOrigin;

import java.time.Duration;


@RequiredArgsConstructor
public class ProxyEndpoint implements GenericFutureListener<Future<Channel>> {
    private final NettyOrigin origin;
    private ChannelHandlerContext channelCtx;

    private Channel originConn;
    private Object msg;
    public void apply(final Object msg, final ChannelHandlerContext channelCtx) {
        this.channelCtx = channelCtx;
        proxyRequestToOrigin(msg);
    }

    private void proxyRequestToOrigin(Object msg) {
        this.msg = msg;

        try {
            Promise<Channel> promise = origin.connectToOrigin(channelCtx.channel());

            if (promise.isDone()) {
                operationComplete(promise);
            } else {
                promise.addListener(this);
            }
        } catch (Exception ex) {
            channelCtx.fireExceptionCaught(ex);
        }
    }


    @Override
    public void operationComplete(final Future<Channel> connectResult) throws Exception {
        if (connectResult.isSuccess()) {
            onOriginConnectSucceeded(connectResult.getNow());
        } else {
            onOriginConnectFailed(connectResult.cause());
        }
    }

    private void onOriginConnectSucceeded(Channel conn) {
        writeClientRequestToOrigin(conn);
    }

    private void writeClientRequestToOrigin(Channel conn) {
        conn.write(msg);
        conn.flush();

        //Get ready to read origin's response
        conn.read();
        originConn = conn;
        channelCtx.read();
    }

    private void onOriginConnectFailed(Throwable cause) {
        channelCtx.fireExceptionCaught(cause);
    }
}
