package org.xyz.proxy.net.proto.mysql;

// MySQL消息流
public class MySQLMessageStream {
    public static final long NULL_LENGTH_ENCODED_INTEGER = -1;
    public static final byte[] EMPTY_BYTES = new byte[0];

    // 存储mysql消息流的字节数组
    private final byte[] data;
    // mysql消息流的长度(Byte，存储mysql消息流的字节数组的大小)
    private final int length;
    // mysql消息流的当前读取索引
    private int position;

    public MySQLMessageStream(byte[] data) {
        this.data = data;
        this.length = data.length;
        this.position = 0;
    }
}
