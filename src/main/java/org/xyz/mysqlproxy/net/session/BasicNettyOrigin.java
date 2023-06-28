package org.xyz.mysqlproxy.net.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.mysqlproxy.net.server.NettyClientConnectionFactory;

import java.net.SocketAddress;

@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class BasicNettyOrigin implements NettyOrigin {
    private final NettyClientConnectionFactory clientConnectionFactory;
    private final SocketAddress socketAddress;

    @Override
    public Promise<Channel> connectToOrigin(final Channel frontChannel) {
        Promise<Channel> promise = frontChannel.eventLoop().newPromise();
        tryMakingNewConnection(frontChannel, promise);
        return promise;
    }

    private void tryMakingNewConnection(final Channel frontChannel, final Promise<Channel> promise) {
        try {
            final ChannelFuture cf = connectToServer(frontChannel);

            if (cf.isDone()) {
                handleConnectCompletion(cf, promise);
            } else {
                cf.addListener(future -> {
                    try {
                        handleConnectCompletion((ChannelFuture) future, promise);
                    } catch (Throwable e) {
                        if (! promise.isDone()) {
                            promise.setFailure(e);
                        }
                        log.warn("Error creating new connection! origin={}", socketAddress.toString());
                    }
                });
            }
        }  catch (Throwable e) {
            promise.setFailure(e);
        }

    }

    private void handleConnectCompletion(final ChannelFuture cf, final Promise<Channel> callerPromise) {
        if (cf.isSuccess()) {
            callerPromise.setSuccess(cf.channel());
        }
        else {
            callerPromise.setFailure(new RuntimeException(cf.cause().getMessage(), cf.cause()));
        }
    }

    private ChannelFuture connectToServer(final Channel frontChannel) {
        return clientConnectionFactory.connect(frontChannel, socketAddress);
    }
}
