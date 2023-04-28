package org.xyz.dbproxy.proxy.frontend.state;

import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

/**
 * Proxy state.
 */
public interface ProxyState {

    /**
     * Execute command.
     *
     * @param context channel handler context
     * @param message message
     * @param databaseProtocolFrontendEngine database protocol frontend engine
     * @param connectionSession connection session
     */
    void execute(ChannelHandlerContext context, Object message, DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, ConnectionSession connectionSession);
}

