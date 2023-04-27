package org.xyz.dbproxy.proxy.frontend.spi;

import io.netty.channel.Channel;
import org.xyz.dbproxy.db.protocol.codec.DatabasePacketCodecEngine;
import org.xyz.dbproxy.infra.util.spi.type.typed.TypedSPI;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;
import org.xyz.dbproxy.proxy.frontend.authentication.AuthenticationEngine;
import org.xyz.dbproxy.proxy.frontend.command.CommandExecuteEngine;

/**
 * Database protocol frontend engine.
 */
public interface DatabaseProtocolFrontendEngine extends TypedSPI {

    /**
     * Initialize channel.
     *
     * @param channel channel
     */
    default void initChannel(Channel channel) {
    }

    /**
     * Set database version.
     *
     * @param databaseName database name
     * @param databaseVersion database version
     */
    default void setDatabaseVersion(String databaseName, String databaseVersion) {
    }

    /**
     * Get database packet codec engine.
     *
     * @return database packet codec engine
     */
    DatabasePacketCodecEngine<?> getCodecEngine();

    /**
     * Get authentication engine.
     *
     * @return authentication engine
     */
    AuthenticationEngine getAuthenticationEngine();

    /**
     * Get command execute engine.
     *
     * @return command execute engine
     */
    CommandExecuteEngine getCommandExecuteEngine();

    /**
     * Release resource.
     *
     * @param connectionSession connection session
     */
    void release(ConnectionSession connectionSession);

    /**
     * Handle exception.
     *
     * @param connectionSession connection session
     * @param exception exception
     */
    void handleException(ConnectionSession connectionSession, Exception exception);
}