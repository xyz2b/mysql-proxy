package org.xyz.proxy.net.connection;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.constants.CharacterSet;
import org.xyz.proxy.net.session.FrontendSession;

@Data
public class FrontendConnection {
    public static final AttributeKey<FrontendConnection> FRONTEND_CONNECTION =
            AttributeKey.valueOf("FRONTEND_CONNECTION");

    private long id;

    private String user;
    private String host;
    private int port;
    private String database;

    private long serverCapabilities;
    private long clientCapabilities;

    private int charsetIndex;
    private String charset;


    private FrontendSession session;

    private Channel frontChannel;

    private BackendConnection backendConnection;

    public FrontendConnection() {
        this.serverCapabilities = serverCapabilities();
    }

    public int getCharsetIndex() {
        return charsetIndex;
    }

    public boolean setCharsetIndex(int index) {
        String charset = CharacterSet.getCharset(index);
        if (charset != null) {
            this.charset = charset;
            this.charsetIndex = index;
            return true;
        } else {
            return false;
        }
    }

    public String getCharset() {
        return charset;
    }

    public boolean setCharset(String charset) {
        int index = CharacterSet.getIndex(charset);
        if (index > 0) {
            this.charset = charset;
            this.charsetIndex = index;
            return true;
        } else {
            return false;
        }
    }

    private long serverCapabilities() {
        long flag = 0;
        flag |= CapabilitiesFlags.CLIENT_LONG_PASSWORD;
        flag |= CapabilitiesFlags.CLIENT_FOUND_ROWS;
        flag |= CapabilitiesFlags.CLIENT_LONG_FLAG;
        flag |= CapabilitiesFlags.CLIENT_CONNECT_WITH_DB;
        flag |= CapabilitiesFlags.CLIENT_NO_SCHEMA;
        flag |= CapabilitiesFlags.CLIENT_COMPRESS;
        flag |= CapabilitiesFlags.CLIENT_ODBC;
        flag |= CapabilitiesFlags.CLIENT_LOCAL_FILES;
        flag |= CapabilitiesFlags.CLIENT_IGNORE_SPACE;
        flag |= CapabilitiesFlags.CLIENT_PROTOCOL_41;
        flag |= CapabilitiesFlags.CLIENT_INTERACTIVE;
        flag |= CapabilitiesFlags.CLIENT_SSL;
        flag |= CapabilitiesFlags.CLIENT_IGNORE_SIGPIPE;
        flag |= CapabilitiesFlags.CLIENT_TRANSACTIONS;
        flag |= CapabilitiesFlags.CLIENT_RESERVED;
        flag |= CapabilitiesFlags.CLIENT_RESERVED2;
        flag |= CapabilitiesFlags.CLIENT_MULTI_STATEMENTS;
        flag |= CapabilitiesFlags.CLIENT_MULTI_RESULTS;
        flag |= CapabilitiesFlags.CLIENT_PS_MULTI_RESULTS;
        flag |= CapabilitiesFlags.CLIENT_PLUGIN_AUTH;
        flag |= CapabilitiesFlags.CLIENT_CONNECT_ATTRS;
        flag |= CapabilitiesFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA;
        flag |= CapabilitiesFlags.CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS;
        flag |= CapabilitiesFlags.CLIENT_SESSION_TRACK;
        flag |= CapabilitiesFlags.CLIENT_DEPRECATE_EOF;
        flag |= CapabilitiesFlags.CLIENT_OPTIONAL_RESULTSET_METADATA;
        flag |= CapabilitiesFlags.CLIENT_ZSTD_COMPRESSION_ALGORITHM;
        flag |= CapabilitiesFlags.CLIENT_QUERY_ATTRIBUTES;
//        flag |= CapabilitiesFlags.MULTI_FACTOR_AUTHENTICATION;
        flag |= CapabilitiesFlags.CLIENT_SSL_VERIFY_SERVER_CERT;
        flag |= CapabilitiesFlags.CLIENT_REMEMBER_OPTIONS;
        return flag;
    }
}
