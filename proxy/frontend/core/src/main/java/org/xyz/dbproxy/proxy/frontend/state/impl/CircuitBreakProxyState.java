package org.xyz.dbproxy.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.db.protocol.packet.DatabasePacket;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.xyz.dbproxy.proxy.frontend.state.ProxyState;

import java.util.Optional;

/**
 * Circuit break proxy state.
 */
public final class CircuitBreakProxyState implements ProxyState {

    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(new CircuitBreakException()));
        Optional<DatabasePacket<?>> databasePacket = databaseProtocolFrontendEngine.getCommandExecuteEngine().getOtherPacket(connectionSession);
        databasePacket.ifPresent(context::writeAndFlush);
    }
}
