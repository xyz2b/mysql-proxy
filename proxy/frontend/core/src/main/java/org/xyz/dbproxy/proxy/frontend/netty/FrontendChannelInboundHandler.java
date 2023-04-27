package org.xyz.dbproxy.proxy.frontend.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.dbproxy.db.protocol.constant.CommonConstants;
import org.xyz.dbproxy.db.protocol.payload.PacketPayload;
import org.xyz.dbproxy.infra.database.type.DatabaseType;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPILoader;
import org.xyz.dbproxy.proxy.backend.context.ProxyContext;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;
import org.xyz.dbproxy.proxy.frontend.authentication.AuthenticationResult;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

import java.util.Optional;

/**
 * Frontend channel inbound handler.
 */
@Slf4j
public final class FrontendChannelInboundHandler extends ChannelInboundHandlerAdapter {

    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;

    private final ConnectionSession connectionSession;

    private volatile boolean authenticated;

    /**
     * @param databaseProtocolFrontendEngine 数据库引擎
     * @param channel 客户端连接channel
     * */
    public FrontendChannelInboundHandler(final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine, final Channel channel) {
        this.databaseProtocolFrontendEngine = databaseProtocolFrontendEngine;
        connectionSession = new ConnectionSession(TypedSPILoader.getService(DatabaseType.class, databaseProtocolFrontendEngine.getType()),
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class).getDefaultType(), channel);
    }

    @Override
    public void channelActive(final ChannelHandlerContext context) {
        int connectionId = databaseProtocolFrontendEngine.getAuthenticationEngine().handshake(context);
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        connectionSession.setConnectionId(connectionId);
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        if (!authenticated) {
            authenticated = authenticate(context, (ByteBuf) message);
            return;
        }
        ProxyStateContext.execute(context, message, databaseProtocolFrontendEngine, connectionSession);
    }

    private boolean authenticate(final ChannelHandlerContext context, final ByteBuf message) {
        try (PacketPayload payload = databaseProtocolFrontendEngine.getCodecEngine().createPacketPayload(message, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get())) {
            AuthenticationResult authResult = databaseProtocolFrontendEngine.getAuthenticationEngine().authenticate(context, payload);
            if (authResult.isFinished()) {
                connectionSession.setGrantee(new Grantee(authResult.getUsername(), authResult.getHostname()));
                connectionSession.setCurrentDatabase(authResult.getDatabase());
                connectionSession.setExecutionId(new ExecuteProcessEngine().initializeConnection(connectionSession.getGrantee(), connectionSession.getDatabaseName()));
            }
            return authResult.isFinished();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            if (!ExpectedExceptions.isExpected(ex.getClass())) {
                log.error("Exception occur: ", ex);
            } else if (log.isDebugEnabled()) {
                log.debug("Exception occur: ", ex);
            }
            context.writeAndFlush(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(ex));
            context.close();
        }
        return false;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        UserExecutorGroup.getInstance().getExecutorService().execute(this::closeAllResources);
    }

    private void closeAllResources() {
        ConnectionThreadExecutorGroup.getInstance().unregisterAndAwaitTermination(connectionSession.getConnectionId());
        connectionSession.getBackendConnection().closeAllResources();
        Optional.ofNullable(connectionSession.getExecutionId()).ifPresent(new ExecuteProcessEngine()::finishConnection);
        databaseProtocolFrontendEngine.release(connectionSession);
    }

    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        if (context.channel().isWritable()) {
            connectionSession.getBackendConnection().getResourceLock().doNotify();
        }
    }
}
