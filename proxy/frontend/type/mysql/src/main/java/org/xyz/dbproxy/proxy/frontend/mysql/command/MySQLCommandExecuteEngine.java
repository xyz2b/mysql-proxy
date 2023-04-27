package org.xyz.dbproxy.proxy.frontend.mysql.command;

import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.proxy.frontend.command.CommandExecuteEngine;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command execute engine for MySQL.
 * MYSQL SQL命令执行引擎
 */
public final class MySQLCommandExecuteEngine implements CommandExecuteEngine {

    @Override
    public MySQLCommandPacketType getCommandPacketType(final PacketPayload payload) {
        return MySQLCommandPacketTypeLoader.getCommandPacketType((MySQLPacketPayload) payload);
    }

    @Override
    public MySQLCommandPacket getCommandPacket(final PacketPayload payload, final CommandPacketType type, final ConnectionSession connectionSession) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        return MySQLCommandPacketFactory.newInstance((MySQLCommandPacketType) type, (MySQLPacketPayload) payload, connectionSession, sqlParserRule.isSqlCommentParseEnabled());
    }

    @Override
    public CommandExecutor getCommandExecutor(final CommandPacketType type, final CommandPacket packet, final ConnectionSession connectionSession) throws SQLException {
        return MySQLCommandExecutorFactory.newInstance((MySQLCommandPacketType) type, packet, connectionSession);
    }

    @Override
    public DatabasePacket<?> getErrorPacket(final Exception cause) {
        return MySQLErrPacketFactory.newInstance(cause);
    }

    @Override
    public Optional<DatabasePacket<?>> getOtherPacket(final ConnectionSession connectionSession) {
        return Optional.empty();
    }

    @Override
    public void writeQueryData(final ChannelHandlerContext context,
                               final BackendConnection backendConnection, final QueryCommandExecutor queryCommandExecutor, final int headerPackagesCount) throws SQLException {
        if (ResponseType.QUERY != queryCommandExecutor.getResponseType() || !context.channel().isActive()) {
            return;
        }
        int count = 0;
        int flushThreshold = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandExecutor.next()) {
            count++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                context.flush();
                backendConnection.getResourceLock().doAwait();
            }
            DatabasePacket<?> dataValue = queryCommandExecutor.getQueryRowPacket();
            context.write(dataValue);
            if (flushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
        context.write(new MySQLEofPacket(ServerStatusFlagCalculator.calculateFor(backendConnection.getConnectionSession())));
    }
}
