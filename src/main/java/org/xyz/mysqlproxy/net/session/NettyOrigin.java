package org.xyz.mysqlproxy.net.session;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.Promise;


public interface NettyOrigin {
    Promise<Channel> connectToOrigin(final Channel frontChannel);
}
