package org.xyz.dbproxy.proxy.frontend.command;

import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.db.protocol.packet.DatabasePacket;
import org.xyz.dbproxy.db.protocol.payload.PacketPayload;
import org.xyz.dbproxy.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command execute engine.
 */
public interface CommandExecuteEngine {

    /**
     * Get command packet type.
     *
     * @param packetPayload packet payload
     * @return command packet type
     */
    CommandPacketType getCommandPacketType(PacketPayload packetPayload);

    /**
     * Get command packet.
     *
     * @param payload packet payload
     * @param type command packet type
     * @param connectionSession connection session
     * @return command packet
     */
    CommandPacket getCommandPacket(PacketPayload payload, CommandPacketType type, ConnectionSession connectionSession);

    /**
     * Get command executor.
     *
     * @param type command packet type
     * @param packet command packet
     * @param connectionSession connection session
     * @return command executor
     * @throws SQLException SQL exception
     */
    CommandExecutor getCommandExecutor(CommandPacketType type, CommandPacket packet, ConnectionSession connectionSession) throws SQLException;

    /**
     * Get error packet.
     *
     * @param cause cause of error
     * @return error packet
     */
    DatabasePacket<?> getErrorPacket(Exception cause);

    /**
     * Get other packet.
     *
     * @param connectionSession connection session
     * @return other packet
     */
    Optional<DatabasePacket<?>> getOtherPacket(ConnectionSession connectionSession);

    /**
     * Write query data.
     *
     * @param context channel handler context
     * @param backendConnection backend connection
     * @param queryCommandExecutor query command executor
     * @param headerPackagesCount count of header packages
     * @throws SQLException SQL exception
     */
    void writeQueryData(ChannelHandlerContext context, BackendConnection backendConnection, QueryCommandExecutor queryCommandExecutor, int headerPackagesCount) throws SQLException;
}
