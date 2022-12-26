package org.xyz.proxy.net.proto.mysql;

public class ErrorPacket extends MySQLPacket {
    public static final byte PACKET_ID = (byte) 0xff;

    public void read(BinaryPacket bin) {

    }

    @Override
    public int calcPacketSize() {
        return 0;
    }

    @Override
    protected String getPacketInfo() {
        return null;
    }
}
