package org.xyz.mysqlproxy.net.constants;

public class CapabilitiesFlags {
    public static final long CLIENT_LONG_PASSWORD = 1;
    public static final long CLIENT_FOUND_ROWS = 1 << 1;
    public static final long CLIENT_LONG_FLAG = 1 << 2;
    public static final long CLIENT_CONNECT_WITH_DB = 1 << 3;
    public static final long CLIENT_NO_SCHEMA = 1 << 4;
    public static final long CLIENT_COMPRESS = 1 << 5;
    public static final long CLIENT_ODBC = 1 << 6;
    public static final long CLIENT_LOCAL_FILES = 1 << 7;
    public static final long CLIENT_IGNORE_SPACE = 1 << 8;
    public static final long CLIENT_PROTOCOL_41 = 1 << 9;
    public static final long CLIENT_INTERACTIVE = 1 << 10;
    public static final long CLIENT_SSL = 1 << 11;
    public static final long CLIENT_IGNORE_SIGPIPE = 1 << 12;
    public static final long CLIENT_TRANSACTIONS = 1 << 13;
    public static final long CLIENT_RESERVED = 1 << 14;
    public static final long CLIENT_RESERVED2 = 1 << 15;
    public static final long CLIENT_MULTI_STATEMENTS = (1L << 16);
    public static final long CLIENT_MULTI_RESULTS = (1L << 17);
    public static final long CLIENT_PS_MULTI_RESULTS = (1L << 18);
    public static final long CLIENT_PLUGIN_AUTH = (1L << 19);
    public static final long CLIENT_CONNECT_ATTRS = (1L << 20);
    public static final long CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = (1L << 21);
    public static final long CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = (1L << 22);
    public static final long CLIENT_SESSION_TRACK = (1L << 23);
    public static final long CLIENT_DEPRECATE_EOF = (1L << 24);
    public static final long CLIENT_OPTIONAL_RESULTSET_METADATA = (1L << 25);
    public static final long CLIENT_ZSTD_COMPRESSION_ALGORITHM = (1L << 26);
    public static final long CLIENT_QUERY_ATTRIBUTES = (1L << 27);
    public static final long MULTI_FACTOR_AUTHENTICATION = (1L << 28);
    public static final long CLIENT_CAPABILITY_EXTENSION = (1L << 29);
    public static final long CLIENT_SSL_VERIFY_SERVER_CERT = (1L << 30);
    public static final long CLIENT_REMEMBER_OPTIONS = (1L << 31);
}
