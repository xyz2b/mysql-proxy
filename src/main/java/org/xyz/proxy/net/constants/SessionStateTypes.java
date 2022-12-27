package org.xyz.proxy.net.constants;

public enum SessionStateTypes {
    SESSION_TRACK_SYSTEM_VARIABLES(0), /**< Session system variables */
    SESSION_TRACK_SCHEMA(1),           /**< Current schema */
    SESSION_TRACK_STATE_CHANGE(2),     /**< track session state changes */
    SESSION_TRACK_GTIDS(3),            /**< See also: session_track_gtids */
    SESSION_TRACK_TRANSACTION_CHARACTERISTICS(4), /**< Transaction chistics */
    SESSION_TRACK_TRANSACTION_STATE(5);            /**< Transaction state */

    private final int value;

    // 构造器默认也只能是private, 从而保证构造函数只能在内部使用
    SessionStateTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
