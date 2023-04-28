package org.xyz.dbproxy.db.protocol.mysql.constant;

/**
 * MySQL connection phase.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase.html">Connection Phase</a>
 */
public enum MySQLConnectionPhase {

    INITIAL_HANDSHAKE, AUTH_PHASE_FAST_PATH, AUTHENTICATION_METHOD_MISMATCH
}

