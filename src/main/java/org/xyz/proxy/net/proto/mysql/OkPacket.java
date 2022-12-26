package org.xyz.proxy.net.proto.mysql;

public class OkPacket extends MySQLPacket {
    public static final byte PACKET_ID = (byte) 0x00;

    @Override
    public int calcPacketSize() {
        return 0;
    }

    @Override
    protected String getPacketInfo() {
        return null;
    }
}
