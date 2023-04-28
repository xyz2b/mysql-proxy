package org.xyz.dbproxy.proxy.frontend.state.impl;

import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.infra.config.props.ConfigurationPropertyKey;
import org.xyz.dbproxy.proxy.backend.context.ProxyContext;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.xyz.dbproxy.proxy.frontend.state.ProxyState;

import java.util.concurrent.ExecutorService;

/**
 * OK proxy state.
 */
public final class OKProxyState implements ProxyState {

    @Override
    public void execute(final ChannelHandlerContext context, final Object message, final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        ExecutorService executorService = determineSuitableExecutorService(connectionSession);
        context.channel().config().setAutoRead(false);
        executorService.execute(new CommandExecutorTask(databaseProtocolFrontendEngine, connectionSession, context, message));
    }

    private ExecutorService determineSuitableExecutorService(final ConnectionSession connectionSession) {
        return requireOccupyThreadForConnection(connectionSession) ? ConnectionThreadExecutorGroup.getInstance().get(connectionSession.getConnectionId())
                : UserExecutorGroup.getInstance().getExecutorService();
    }

    private boolean requireOccupyThreadForConnection(final ConnectionSession connectionSession) {
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)
                || TransactionType.isDistributedTransaction(connectionSession.getTransactionStatus().getTransactionType());
    }
}
