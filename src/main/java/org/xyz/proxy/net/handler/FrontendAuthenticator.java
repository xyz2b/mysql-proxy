package org.xyz.proxy.net.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.xyz.proxy.net.proto.mysql.HandshakePacket;
import org.xyz.proxy.util.RandomUtil;

// 前端通道Active处理器
public class FrontendAuthenticator extends ChannelInboundHandlerAdapter {
    // 盐值
    private byte[] seed;

    // 发送 握手初始化请求
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 生成认证数据
        byte[] rand1 = RandomUtil.randomBytes(8);
        byte[] rand2 = RandomUtil.randomBytes(12);

        // 保存认证数据
        byte[] seed = new byte[rand1.length + rand2.length];
        System.arraycopy(rand1, 0, seed, 0, rand1.length);
        System.arraycopy(rand2, 0, seed, rand1.length, rand2.length);
        this.seed = seed;

        // 握手初始化请求包
        HandshakePacket hs = new HandshakePacket();
        hs.setSequenceId((byte) 0);

        hs.write(ctx);
    }

    // 接收 握手响应，认证用户和密码
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }
}
