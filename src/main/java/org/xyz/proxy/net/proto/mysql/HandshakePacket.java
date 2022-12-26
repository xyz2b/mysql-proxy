package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.proto.util.ByteWriterUtil;

@Data
public class HandshakePacket extends MySQLPacket {
    private static final byte[] RESERVED = new byte[10];
    private static final byte[] FILLER_1 = new byte[1];

    private int protocolVersion;
    private String serverVersion;
    private long threadId;
    // auth-plugin-data-part1(8字节) + auth-plugin-data-part-2(如果serverCapabilities的CLIENT_PLUGIN_AUTH置位，auth_plugin_data_len-8字节)
    // 总长度等于 auth_plugin_data_len(如果serverCapabilities的CLIENT_PLUGIN_AUTH置位，否则为8字节)
    private byte[] seed;
    private int serverCapabilities;
    private int characterSetIndex;
    public int serverStatus;
    private String authPluginName;

    public HandshakePacket() {

    }

    public void read(BinaryPacket bin) {
        setPayloadLength(bin.getPayloadLength());
        setSequenceId(bin.getSequenceId());

        MySQLMessageStream mm = new MySQLMessageStream(bin.payload);
        protocolVersion = mm.readUB1();
        serverVersion = mm.readStringWithNull();
        threadId = mm.readUB4();

        byte[] seedPart1 = mm.readBytes(8);

        mm.inc(1);  // filter, 0x00
        serverCapabilities = mm.readUB2();
        characterSetIndex = mm.readUB1();
        serverStatus = mm.readUB2();
        serverCapabilities = serverCapabilities | mm.readUB2();

        int authPluginDataLen = 0;
        if((serverCapabilities & CapabilitiesFlags.CLIENT_PLUGIN_AUTH) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH) {
            authPluginDataLen = mm.readUB1();
        } else {
            mm.inc(1);  // 0x00
        }

        mm.inc(10);

        byte[] seedPart2 = new byte[0];
        if(authPluginDataLen > 0) {
            int seedPart2Length = Math.max(13, authPluginDataLen - 8);
            seedPart2 = mm.readBytes(seedPart2Length);
        }
        if((serverCapabilities & CapabilitiesFlags.CLIENT_PLUGIN_AUTH) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH) {
            authPluginName = mm.readStringWithNull();
        }

        seed = new byte[seedPart1.length + seedPart2.length];
        System.arraycopy(seedPart1, 0, seed, 0, seedPart1.length);
        System.arraycopy(seedPart2, 0, seed, seedPart1.length, seedPart2.length);
    }

    public void write(final ChannelHandlerContext ctx) {
        // default init 256,so it can avoid buff extract
        final ByteBuf buffer = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(buffer, calcPacketSize());
        ByteWriterUtil.writeUB1(buffer, getSequenceId());

        ByteWriterUtil.writeUB1(buffer, protocolVersion);
        ByteWriterUtil.writeBytesWithNull(buffer, serverVersion.getBytes());
        ByteWriterUtil.writeUB4(buffer, threadId);
        ByteWriterUtil.writeBytes(buffer, seed, 0, 8);
        buffer.writeBytes(FILLER_1);
        ByteWriterUtil.writeUB2(buffer, serverCapabilities & 0xFFFF);
        ByteWriterUtil.writeUB1(buffer, characterSetIndex);
        ByteWriterUtil.writeUB2(buffer, serverStatus);
        ByteWriterUtil.writeUB2(buffer, (serverCapabilities >> 16) & 0xFFFF);
        if((serverCapabilities & CapabilitiesFlags.CLIENT_PLUGIN_AUTH) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH) {
            ByteWriterUtil.writeUB1(buffer, seed.length);
        } else {
            ByteWriterUtil.writeUB1(buffer, 0x00);
        }
        buffer.writeBytes(RESERVED);
        ByteWriterUtil.writeBytes(buffer, seed, 8, seed.length);

        if((serverCapabilities & CapabilitiesFlags.CLIENT_PLUGIN_AUTH) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH) {
            ByteWriterUtil.writeBytesWithNull(buffer, authPluginName.getBytes());
        }
        ctx.writeAndFlush(buffer);
    }

    @Override
    public int calcPacketSize() {
        int size = 1;   // protocol version
        size += serverVersion.length() + 1; // serverVersion + null
        size += 4; // threadId
        size += seed.length; // auth-plugin-data-part-1 + auth-plugin-data-part-2
        size += FILLER_1.length; // filler 0x00
        size += 4; // capability_flags
        size += 1; // character_set
        size += 2; // status_flags
        size += 1;  // auth_plugin_data_len or 0x00
        size += 10; // reserved
        size += authPluginName != null ? authPluginName.length() + 1 : 0;   // auth_plugin_name + null
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Handshake Packet";
    }
}
