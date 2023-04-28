package org.xyz.dbproxy.proxy.frontend.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.xyz.dbproxy.db.protocol.constant.CommonConstants;
import org.xyz.dbproxy.db.protocol.payload.PacketPayload;
import org.xyz.dbproxy.infra.database.type.DatabaseType;
import org.xyz.dbproxy.infra.metadata.user.Grantee;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPILoader;
import org.xyz.dbproxy.proxy.backend.context.ProxyContext;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;
import org.xyz.dbproxy.proxy.frontend.authentication.AuthenticationResult;
import org.xyz.dbproxy.proxy.frontend.executor.ConnectionThreadExecutorGroup;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.xyz.dbproxy.proxy.frontend.state.ProxyStateContext;

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
        // 发送握手请求报文
        int connectionId = databaseProtocolFrontendEngine.getAuthenticationEngine().handshake(context);
        // 将当前连接关联到一个线程上
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        connectionSession.setConnectionId(connectionId);
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {
        // 没有认证的要进行认证，已经认证过了，就直接进行后续的处理
        if (!authenticated) {
            authenticated = authenticate(context, (ByteBuf) message);
            return;
        }
        ProxyStateContext.execute(context, message, databaseProtocolFrontendEngine, connectionSession);
    }

    // 认证
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
        // Optional 类是一个可以为null的容器对象。如果值存在则isPresent()方法会返回true，调用get()方法会返回该对象。
        // Optional 是个容器：它可以保存类型T的值，或者仅仅保存null。Optional提供很多有用的方法，这样我们就不用显式进行空值检测。
        // Optional 类的引入很好的解决空指针异常。
        // ifPresent: 当connectionSession.getExecutionId() 存在时 则进行后续操作，否则不进行操作
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
