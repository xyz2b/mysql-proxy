package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.util.ByteWriterUtil;

@Data
public class ErrorPacketDef extends MySQLPacketDef {
    public static final byte PACKET_ID = (byte) 0xff;

    private static final byte SQLSTATE_MARKER = (byte) '#';
    private static final byte[] DEFAULT_SQLSTATE = "HY000".getBytes();

    private byte header = PACKET_ID;
    private int errorCode;
    private String sqlStateMarker;
    private String sqlState;
    private String errorMessage;

    private long serverCapabilities;
    public ErrorPacketDef(long serverCapabilities) {
        this.serverCapabilities = serverCapabilities;
    }

    public void read(BinaryPacketDef bin) {
        setPayloadLength(bin.getPayloadLength());
        setSequenceId(bin.getSequenceId());
        MySQLMessageStream mm = new MySQLMessageStream(bin.getPayload());
        header = (byte) mm.readUB1();
        errorCode = mm.readUB2();
        if ((serverCapabilities & CapabilitiesFlags.CLIENT_PROTOCOL_41) == CapabilitiesFlags.CLIENT_PROTOCOL_41) {
            sqlStateMarker = mm.readStringByLength(1);
            sqlState = mm.readStringByLength(5);
        }
        errorMessage = mm.readString();
    }

    public void write(ChannelHandlerContext ctx) {
        // default 256 , no need to check and auto expand
        ByteBuf buffer = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(buffer, calcPacketSize());
        ByteWriterUtil.writeUB1(buffer, getSequenceId());

        ByteWriterUtil.writeUB1(buffer, header);
        ByteWriterUtil.writeUB2(buffer, errorCode);
        if ((serverCapabilities & CapabilitiesFlags.CLIENT_PROTOCOL_41) == CapabilitiesFlags.CLIENT_PROTOCOL_41) {
            ByteWriterUtil.writeBytes(buffer, sqlStateMarker.getBytes());
            ByteWriterUtil.writeBytes(buffer, sqlState.getBytes());
        }
        ByteWriterUtil.writeBytes(buffer, errorMessage.getBytes());
        ctx.writeAndFlush(buffer);
    }

    @Override
    public int calcPacketSize() {
        int size = 1; // header
        size += 2; // error_code
        if ((serverCapabilities & CapabilitiesFlags.CLIENT_PROTOCOL_41) == CapabilitiesFlags.CLIENT_PROTOCOL_41) {
            size += 1;  // sql_state_marker
            size += 5;  // sql_state
        }
        size += errorMessage.length();  // error_message
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Error Packet";
    }
}
