package org.xyz.dbproxy.db.protocol.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.xyz.dbproxy.db.protocol.constant.CommonConstants;

import java.nio.charset.Charset;

/**
 * Channel attributes initializer.
 */
public final class ChannelAttrInitializer extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // 设置channel的charset属性，使用JVM默认的charset，不设置的话默认跟随系统
        ctx.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).setIfAbsent(Charset.defaultCharset());
        ctx.fireChannelActive();
    }
}
