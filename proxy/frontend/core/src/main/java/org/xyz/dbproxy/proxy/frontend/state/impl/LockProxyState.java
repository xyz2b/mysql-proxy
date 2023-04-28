package org.xyz.dbproxy.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.xyz.dbproxy.proxy.frontend.state.ProxyState;

/**
 * Lock proxy state.
 */
public final class LockProxyState implements ProxyState {

    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        throw new UnsupportedSQLOperationException("LockProxyState");
    }
}

