package org.xyz.dbproxy.proxy.frontend.state;

import io.netty.channel.ChannelHandlerContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xyz.dbproxy.infra.state.instance.InstanceState;
import org.xyz.dbproxy.proxy.backend.context.ProxyContext;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.xyz.dbproxy.proxy.frontend.state.impl.CircuitBreakProxyState;
import org.xyz.dbproxy.proxy.frontend.state.impl.LockProxyState;
import org.xyz.dbproxy.proxy.frontend.state.impl.OKProxyState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy state context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyStateContext {

    private static final Map<InstanceState, ProxyState> STATES = new ConcurrentHashMap<>(3, 1);

    static {
        STATES.put(InstanceState.OK, new OKProxyState());
        STATES.put(InstanceState.LOCK, new LockProxyState());
        STATES.put(InstanceState.CIRCUIT_BREAK, new CircuitBreakProxyState());
    }

    /**
     * Execute command.
     *
     * @param context channel handler context
     * @param message message
     * @param databaseProtocolFrontendEngine database protocol frontend engine
     * @param connectionSession connection session
     */
    public static void execute(final ChannelHandlerContext context, final Object message,
                               final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final ConnectionSession connectionSession) {
        // ifPresent: 当 ProxyContext.getInstance().getInstanceStateContext() 存在时 则进行后续操作（传入的optional就是ProxyContext.getInstance().getInstanceStateContext()），否则不进行操作
        ProxyContext.getInstance().getInstanceStateContext().ifPresent(optional -> STATES.get(optional.getCurrentState()).execute(context, message, databaseProtocolFrontendEngine, connectionSession));
    }
}
