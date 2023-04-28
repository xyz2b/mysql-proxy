package org.xyz.dbproxy.db.protocol.mysql.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLConstants;

/**
 * Handle MySQL sequence ID before sending to downstream.
 */
public final class MySQLSequenceIDInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        short sequenceId = byteBuf.readUnsignedByte();
        context.channel().attr(MySQLConstants.MYSQL_SEQUENCE_ID).get().set(sequenceId + 1);
        context.fireChannelRead(byteBuf.readSlice(byteBuf.readableBytes()));
    }
}
