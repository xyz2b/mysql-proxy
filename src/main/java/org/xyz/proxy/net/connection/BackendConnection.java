package org.xyz.proxy.net.connection;

import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;

@Data
public class BackendConnection {

    private long clientFlag;

    public BackendConnection() {
        this.clientFlag = clientFlag();
    }

    /**
     * 与MySQL连接时的一些特性指定
     */
    private static long clientFlag() {
        int flag = 0;
        flag |= CapabilitiesFlags.CLIENT_LONG_PASSWORD;
        flag |= CapabilitiesFlags.CLIENT_FOUND_ROWS;
        flag |= CapabilitiesFlags.CLIENT_LONG_FLAG;
        flag |= CapabilitiesFlags.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= CapabilitiesFlags.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= CapabilitiesFlags.CLIENT_IGNORE_SPACE;
        flag |= CapabilitiesFlags.CLIENT_PROTOCOL_41;
        flag |= CapabilitiesFlags.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= CapabilitiesFlags.CLIENT_IGNORE_SIGPIPE;
        flag |= CapabilitiesFlags.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        // client extension
        // 不允许MULTI协议
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }
}
