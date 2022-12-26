package org.xyz.proxy.net.connection;

import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.constants.CharacterSet;
import org.xyz.proxy.net.session.FrontendSession;

@Data
public class FrontendConnection {
    protected long id;

    protected String user;
    protected String host;
    protected int port;

    private long serverCapabilities;
    private long clientCapabilities;

    protected int charsetIndex;
    protected String charset;


    private FrontendSession session;

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
        // flag |= ServerDefs.CLIENT_RESERVED;
        return flag;
    }
}
