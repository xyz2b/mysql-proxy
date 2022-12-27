package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.constants.StatusFlags;
import org.xyz.proxy.net.proto.util.ByteReaderUtil;
import org.xyz.proxy.net.proto.util.ByteWriterUtil;

public class OkPacket extends MySQLPacket {
    public static final byte PACKET_ID = (byte) 0x00;

    public static final byte[] OK_PACKET = new byte[] { 7, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0 };
    public static final byte[] AUTH_OK_PACKET = new byte[] { 7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0 };

    private byte header = PACKET_ID;
    private long affectedRows;
    private long lastInsertId;
    private int statusFlags;
    private int warningCount;
    private String info;
    private String sessionStateInfo;

    private long serverCapabilities;
    public OkPacket(long serverCapabilities) {
        this.serverCapabilities = serverCapabilities;
    }

    public void read(BinaryPacket bin) {
        setPayloadLength(bin.getPayloadLength());
        setSequenceId(bin.getSequenceId());
        MySQLMessageStream mm = new MySQLMessageStream(bin.getPayload());
        header = (byte) mm.readUB1();
        affectedRows = mm.readLengthEncodedInteger();
        lastInsertId = mm.readLengthEncodedInteger();
        statusFlags = mm.readUB2();
        if((serverCapabilities & CapabilitiesFlags.CLIENT_PROTOCOL_41) == CapabilitiesFlags.CLIENT_PROTOCOL_41) {
            warningCount = mm.readUB2();
        }
        if((serverCapabilities & CapabilitiesFlags.CLIENT_SESSION_TRACK) == CapabilitiesFlags.CLIENT_SESSION_TRACK) {
            info = mm.readString();
            if((statusFlags & StatusFlags.SERVER_SESSION_STATE_CHANGED) == StatusFlags.SERVER_SESSION_STATE_CHANGED) {
                sessionStateInfo = mm.readString();
            }
        } else {
            info = mm.readString();
        }
    }

    public void write(ChannelHandlerContext ctx) {
        // default init 256,so it can avoid buff extract
        ByteBuf buffer = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(buffer, calcPacketSize());
        ByteWriterUtil.writeUB1(buffer, getSequenceId() & 0xFF);

        ByteWriterUtil.writeUB1(buffer, header);
        ByteWriterUtil.writeLengthEncodedInteger(buffer, affectedRows);
        ByteWriterUtil.writeLengthEncodedInteger(buffer, lastInsertId);
        ByteWriterUtil.writeUB2(buffer, statusFlags);
        if((serverCapabilities & CapabilitiesFlags.CLIENT_PROTOCOL_41) == CapabilitiesFlags.CLIENT_PROTOCOL_41) {
            ByteWriterUtil.writeUB2(buffer, warningCount);
        }
        if((serverCapabilities & CapabilitiesFlags.CLIENT_SESSION_TRACK) == CapabilitiesFlags.CLIENT_SESSION_TRACK) {
            ByteWriterUtil.writeBytesWithLength(buffer, info.getBytes());
            if((statusFlags & StatusFlags.SERVER_SESSION_STATE_CHANGED) == StatusFlags.SERVER_SESSION_STATE_CHANGED) {
                ByteWriterUtil.writeBytesWithLength(buffer, sessionStateInfo.getBytes());
            }
        } else {
            ByteWriterUtil.writeBytes(buffer, info.getBytes());
        }
        ctx.writeAndFlush(buffer);
    }

    @Override
    public int calcPacketSize() {
        int i = 1;      // header length
        i += ByteReaderUtil.getLengthWidth(affectedRows);       // affected_rows length
        i += ByteReaderUtil.getLengthWidth(lastInsertId);       // last_insert_id length
        i += 2;     // status_flags length
        if((serverCapabilities & CapabilitiesFlags.CLIENT_PROTOCOL_41) == CapabilitiesFlags.CLIENT_PROTOCOL_41) {
            i += 2; // warnings length
        }
        if((serverCapabilities & CapabilitiesFlags.CLIENT_SESSION_TRACK) == CapabilitiesFlags.CLIENT_SESSION_TRACK) {
            i += (info.length() + ByteReaderUtil.getLengthWidth(info.getBytes()));    // info length + info length width
            if((statusFlags & StatusFlags.SERVER_SESSION_STATE_CHANGED) == StatusFlags.SERVER_SESSION_STATE_CHANGED) {
                i += (sessionStateInfo.length() + ByteReaderUtil.getLengthWidth(sessionStateInfo.getBytes()));    // session state info length + session state info length width
            }
        } else {
            i += info.length(); // info length
        }
        return i;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL OK Packet";
    }

}
