package org.xyz.dbproxy.proxy.frontend.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xyz.dbproxy.db.protocol.codec.PacketCodec;
import org.xyz.dbproxy.db.protocol.netty.ChannelAttrInitializer;
import org.xyz.dbproxy.infra.database.type.DatabaseType;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPILoader;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Server handler initializer.
 */
@RequiredArgsConstructor
@Slf4j
public final class ServerHandlerInitializer extends ChannelInitializer<Channel> {

    // 数据库的类型（mysql）
    private final DatabaseType databaseType;

    @Override
    protected void initChannel(final Channel socketChannel) {
        // 通过SPI机制根据数据库类型，获取对应的DatabaseProtocolFrontendEngine，DatabaseProtocolFrontendEngine里面包含了对应数据库类型的DatabasePacketCodecEngine、AuthenticationEngine以及CommandExecuteEngine
        DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = TypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, databaseType.getType());
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ChannelAttrInitializer());
        // 根据不同的数据库类型，来选择不同的编解码器
        pipeline.addLast(new PacketCodec(databaseProtocolFrontendEngine.getCodecEngine()));
        pipeline.addLast(FrontendChannelInboundHandler.class.getSimpleName(), new FrontendChannelInboundHandler(databaseProtocolFrontendEngine, socketChannel));
        databaseProtocolFrontendEngine.initChannel(socketChannel);
    }
}

