package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.constants.SessionStateTypes;
import org.xyz.proxy.net.constants.StatusFlags;
import org.xyz.proxy.net.proto.util.ByteReaderUtil;
import org.xyz.proxy.net.proto.util.ByteWriterUtil;

@Data
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

    // Session State Information
    private int type;
    private SessionStateInformation sessionStateInformation;

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
            info = mm.readStringWithLength();
            if((statusFlags & StatusFlags.SERVER_SESSION_STATE_CHANGED) == StatusFlags.SERVER_SESSION_STATE_CHANGED) {
                mm.readLengthEncodedInteger();
                type = mm.readUB1();
                mm.inc(-1); // 因为sessionStateInformation.read 里面也会读取type，这里回退下读取指针
                if(type == SessionStateTypes.SESSION_TRACK_SYSTEM_VARIABLES.getValue()) {
                    sessionStateInformation = new SessionTrackSystemVariables();
                    sessionStateInformation.read(mm);
                } else if (type == SessionStateTypes.SESSION_TRACK_SCHEMA.getValue()) {
                    sessionStateInformation = new SessionTrackSchema();
                    sessionStateInformation.read(mm);
                } else if (type == SessionStateTypes.SESSION_TRACK_STATE_CHANGE.getValue()) {
                    sessionStateInformation = new SessionTrackStateChange();
                    sessionStateInformation.read(mm);
                }
            }
        } else {
            info = mm.readString();
        }
    }

    public void write(ChannelHandlerContext ctx) {
        // default init 256,so it can avoid buff extract
        ByteBuf buffer = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(buffer, calcPacketSize());
        ByteWriterUtil.writeUB1(buffer, getSequenceId());

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
                ByteWriterUtil.writeLengthEncodedInteger(buffer, sessionStateInformation.length());
                sessionStateInformation.write(buffer);
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
                i += ByteReaderUtil.getLengthWidth(sessionStateInformation.length()) + sessionStateInformation.length();    // session state info length
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


    public interface SessionStateInformation {
        void read(MySQLMessageStream mm);
        void write(ByteBuf buf);
        long length();
    }

    @Data
    public static class SessionTrackSystemVariables implements SessionStateInformation {
        private int type = SessionStateTypes.SESSION_TRACK_SYSTEM_VARIABLES.getValue();
        private int mandatoryFlag;
        private String name;
        private String value;

        @Override
        public void read(MySQLMessageStream mm) {
            type = mm.readUB1();
            mandatoryFlag = mm.readUB1();
            name = mm.readStringWithLength();
            value = mm.readStringWithLength();
        }

        @Override
        public void write(ByteBuf buf) {
            ByteWriterUtil.writeUB1(buf, type);
            ByteWriterUtil.writeUB1(buf, mandatoryFlag);
            ByteWriterUtil.writeBytesWithLength(buf, name.getBytes());
            ByteWriterUtil.writeBytesWithLength(buf, value.getBytes());
        }

        @Override
        public long length() {
            int size = 1 + 1; // type + mandatory flag
            size += ByteReaderUtil.getLengthWidth(name.length()) + name.length(); // name length width + name length
            size += ByteReaderUtil.getLengthWidth(value.length()) + value.length(); // value length width + value length
            return size;
        }
    }

    @Data
    public static class SessionTrackSchema implements SessionStateInformation {
        private int type = SessionStateTypes.SESSION_TRACK_SCHEMA.getValue();
        private int mandatoryFlag;
        private String name;

        @Override
        public void read(MySQLMessageStream mm) {
            type = mm.readUB1();
            mandatoryFlag = mm.readUB1();
            name = mm.readStringWithLength();
        }

        @Override
        public void write(ByteBuf buf) {
            ByteWriterUtil.writeUB1(buf, type);
            ByteWriterUtil.writeUB1(buf, mandatoryFlag);
            ByteWriterUtil.writeBytesWithLength(buf, name.getBytes());
        }

        @Override
        public long length() {
            int size = 1 + 1; // type + mandatory flag
            size += ByteReaderUtil.getLengthWidth(name.length()) + name.length(); // name length width + name length
            return size;
        }
    }

    @Data
    public static class SessionTrackStateChange implements SessionStateInformation {
        private int type = SessionStateTypes.SESSION_TRACK_STATE_CHANGE.getValue();
        private int mandatoryFlag;
        private String isTracked;

        @Override
        public void read(MySQLMessageStream mm) {
            type = mm.readUB1();
            mandatoryFlag = mm.readUB1();
            isTracked = mm.readStringWithLength();
        }

        @Override
        public void write(ByteBuf buf) {
            ByteWriterUtil.writeUB1(buf, type);
            ByteWriterUtil.writeUB1(buf, mandatoryFlag);
            ByteWriterUtil.writeBytesWithLength(buf, isTracked.getBytes());
        }

        @Override
        public long length() {
            int size = 1 + 1; // type + mandatory flag
            size += ByteReaderUtil.getLengthWidth(isTracked.length()) + isTracked.length(); // name length width + name length
            return size;
        }
    }

}
