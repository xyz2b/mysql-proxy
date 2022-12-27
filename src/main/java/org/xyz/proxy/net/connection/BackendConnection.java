package org.xyz.proxy.net.connection;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.constants.CharacterSet;

@Data
public class BackendConnection {
    public static final AttributeKey<BackendConnection> BACKEND_CONNECTION =
            AttributeKey.valueOf("BACKEND_CONNECTION");
    private long id;

    private int charsetIndex;
    private String charset;

    private long clientFlag;

    private Channel backendChannel;

    private FrontendConnection frontendConnection;

    public BackendConnection() {
        this.clientFlag = clientFlag();
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

    /**
     * 与MySQL连接时的一些特性指定
     */
    private static long clientFlag() {
        int flag = 0;
        flag |= CapabilitiesFlags.CLIENT_LONG_PASSWORD;
        flag |= CapabilitiesFlags.CLIENT_LONG_FLAG;
        flag |= CapabilitiesFlags.CLIENT_CONNECT_WITH_DB;
        flag |= CapabilitiesFlags.CLIENT_LOCAL_FILES;
        flag |= CapabilitiesFlags.CLIENT_PROTOCOL_41;
        flag |= CapabilitiesFlags.CLIENT_INTERACTIVE;
        flag |= CapabilitiesFlags.CLIENT_TRANSACTIONS;
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
        flag |= CapabilitiesFlags.CLIENT_QUERY_ATTRIBUTES;
        return flag;
    }
}
