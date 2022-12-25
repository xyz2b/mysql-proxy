package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;

@Data
public class HandshakePacket extends MySQLPacket {
    private byte protocolVersion;
    private byte[] serverVersion;
    private long threadId;
    // auth-plugin-data-part1(8字节) + auth-plugin-data-part-2(如果serverCapabilities的CLIENT_PLUGIN_AUTH置位，auth_plugin_data_len-8字节)
    // 总长度等于 auth_plugin_data_len(如果serverCapabilities的CLIENT_PLUGIN_AUTH置位，否则为8字节)
    private byte[] seed;
    private long serverCapabilities;
    private byte characterSet;
    public int serverStatus;
    private byte[] authPluginName;

    public HandshakePacket() {

    }

    public void read(BinaryPacket bin) {

    }

    public void write(final ChannelHandlerContext ctx) {


    }

    @Override
    public int calcPacketSize() {
        int size = 1;   // protocol version
        size += serverVersion.length; // serverVersion
        size += 1;  // serverVersion null
        size += 4; // threadId
        size += seed.length; // auth-plugin-data-part-1 + auth-plugin-data-part-2
        size += 1; // filler 0x00
        size += 4; // capability_flags
        size += 1; // character_set
        size += 2; // status_flags
        size += 1;  // auth_plugin_data_len or 0x00
        size += 10; // reserved
        if(authPluginName != null) {
            size += authPluginName.length; // auth_plugin_name
            size += 1; // auth_plugin_name null
        }
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Handshake Packet";
    }

    public static void main(String[] args) {
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.setServerCapabilities(handshakePacket.getServerCapabilities() | CapabilitiesFlags.CLIENT_PLUGIN_AUTH);
        System.out.println(handshakePacket.getServerCapabilities() & CapabilitiesFlags.CLIENT_PLUGIN_AUTH);
        System.out.println(1L << 19);
    }
}
