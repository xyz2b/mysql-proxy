package org.xyz.proxy.net.constants;

public class StatusFlags {
    public static final short SERVER_STATUS_IN_TRANS = 1;
    public static final short SERVER_STATUS_AUTOCOMMIT = 1 << 1;
    public static final short SERVER_MORE_RESULTS_EXISTS = 1 << 3;
    public static final short SERVER_QUERY_NO_GOOD_INDEX_USED = 1 << 4;
    public static final short SERVER_QUERY_NO_INDEX_USED = 1 << 5;
    public static final short SERVER_STATUS_CURSOR_EXISTS = 1 << 6;
    public static final short SERVER_STATUS_LAST_ROW_SENT = 1 << 7;
    public static final short SERVER_STATUS_DB_DROPPED = 1 << 8;
    public static final short SERVER_STATUS_NO_BACKSLASH_ESCAPES = 1 << 9;
    public static final short SERVER_STATUS_METADATA_CHANGED = 1 << 10;
    public static final short SERVER_QUERY_WAS_SLOW = 1 << 11;
    public static final short SERVER_PS_OUT_PARAMS = 1 << 12;
    public static final short SERVER_STATUS_IN_TRANS_READONLY = 1 << 13;
    public static final short SERVER_SESSION_STATE_CHANGED = 1 << 14;

    public static void main(String[] args) {
        System.out.println(1 << 1);
    }
}
