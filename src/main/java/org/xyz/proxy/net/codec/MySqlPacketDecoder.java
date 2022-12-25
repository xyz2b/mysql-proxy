package org.xyz.proxy.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MySqlPacketDecoder extends LengthFieldBasedFrameDecoder  {
    public MySqlPacketDecoder() {
        super(Integer.MAX_VALUE, 0, 3, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        return null;
    }
}
