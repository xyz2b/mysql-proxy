package org.xyz.dbproxy.proxy.frontend.mysql;

import io.netty.channel.Channel;
import lombok.Getter;
import org.xyz.dbproxy.db.protocol.codec.DatabasePacketCodecEngine;
import org.xyz.dbproxy.db.protocol.mysql.codec.MySQLPacketCodecEngine;
import org.xyz.dbproxy.db.protocol.mysql.constant.MySQLConstants;
import org.xyz.dbproxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;
import org.xyz.dbproxy.proxy.frontend.authentication.AuthenticationEngine;
import org.xyz.dbproxy.proxy.frontend.command.CommandExecuteEngine;
import org.xyz.dbproxy.proxy.frontend.mysql.authentication.MySQLAuthenticationEngine;
import org.xyz.dbproxy.proxy.frontend.mysql.command.MySQLCommandExecuteEngine;
import org.xyz.dbproxy.proxy.frontend.netty.FrontendChannelInboundHandler;
import org.xyz.dbproxy.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Frontend engine for MySQL.
 * 和客户端交互的MYSQL前端引擎。包含MYSQL认证引擎、MYSQL命令执行引擎以及MYSQL编解码引擎
 */
@Getter
public final class MySQLFrontendEngine implements DatabaseProtocolFrontendEngine {

    private final AuthenticationEngine authenticationEngine = new MySQLAuthenticationEngine();

    private final CommandExecuteEngine commandExecuteEngine = new MySQLCommandExecuteEngine();

    private final DatabasePacketCodecEngine<MySQLPacket> codecEngine = new MySQLPacketCodecEngine();

    @Override
    public void initChannel(final Channel channel) {
        // channel对应的MYSQL_SEQUENCE_ID就是这里绑定上的
        channel.attr(MySQLConstants.MYSQL_SEQUENCE_ID).set(new AtomicInteger());
        channel.pipeline().addBefore(FrontendChannelInboundHandler.class.getSimpleName(), MySQLSequenceIDInboundHandler.class.getSimpleName(), new MySQLSequenceIDInboundHandler());
    }

    @Override
    public void setDatabaseVersion(final String databaseName, final String databaseVersion) {
        MySQLServerInfo.setServerVersion(databaseName, databaseVersion);
    }

    @Override
    public void release(final ConnectionSession connectionSession) {
        MySQLStatementIDGenerator.getInstance().unregisterConnection(connectionSession.getConnectionId());
    }

    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
    }

    @Override
    public String getType() {
        return "MySQL";
    }
}