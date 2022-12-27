package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public abstract class MySQLPacket {
    /**
     * none, this is an internal thread state
     */
    public static final byte COM_SLEEP = 0x00;

    /**
     * mysql_close
     */
    public static final byte COM_QUIT = 0x01;

    /**
     * mysql_select_db
     */
    public static final byte COM_INIT_DB = 0x02;

    /**
     * mysql_real_query
     */
    public static final byte COM_QUERY = 0x03;

    /**
     * mysql_list_fields
     */
    public static final byte COM_FIELD_LIST = 0x04;

    /**
     * mysql_refresh
     */
    public static final byte COM_REFRESH = 0x07;
    /**
     * mysql_stat
     */
    public static final byte COM_STATISTICS = 0x08;

    /**
     * mysql_list_processes
     */
    public static final byte COM_PROCESS_INFO = 0x0A;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_CONNECT = 0x0B;

    /**
     * mysql_kill
     */
    public static final byte COM_PROCESS_KILL = 0x0C;

    /**
     * mysql_dump_debug_info
     */
    public static final byte COM_DEBUG = 0x0D;

    /**
     * mysql_ping
     */
    public static final byte COM_PING = 0x0E;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_TIME = 0x0F;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_DELAYED_INSERT = 0x10;

    /**
     * mysql_change_user
     */
    public static final byte COM_CHANGE_USER = 0x11;

    /**
     * reset connection
     * */
    public static final byte COM_RESET_CONNECTION = 0x1F;

    /**
     * mysql_stmt_prepare
     */
    public static final byte COM_STMT_PREPARE = 0x16;

    /**
     * mysql_stmt_execute
     */
    public static final byte COM_STMT_EXECUTE = 0x17;

    /**
     * mysql_stmt_send_long_data
     */
    public static final byte COM_STMT_SEND_LONG_DATA = 0x18;

    /**
     * mysql_stmt_close
     */
    public static final byte COM_STMT_CLOSE = 0x19;

    /**
     * mysql_stmt_reset
     */
    public static final byte COM_STMT_RESET = 0x1A;

    /**
     * mysql_set_server_option
     */
    public static final byte COM_SET_OPTION = 0x1A;

    public static final byte COM_BINLOG_DUMP = 0x12;

    private int payloadLength;
    private int sequenceId = 0;

    public void read(BinaryPacket bin) {
        throw new UnsupportedOperationException();
    }

    /**
     * 把数据包通过后端连接写出，一般使用buffer机制来提高写的吞吐量。
     */
    public void write(ChannelHandlerContext ctx) {
        throw new UnsupportedOperationException();
    }


    /**
     * 计算数据包大小，不包含包头长度。
     */
    public abstract int calcPacketSize();

    /**
     * 取得数据包信息
     */
    protected abstract String getPacketInfo();

    @Override
    public String toString() {
        return new StringBuilder().append(getPacketInfo()).append("{length=").append(payloadLength).append(",id=")
                .append(sequenceId).append('}').toString();
    }
}
