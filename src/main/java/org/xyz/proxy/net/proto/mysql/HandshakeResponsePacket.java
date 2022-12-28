package org.xyz.proxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.xyz.proxy.net.constants.CapabilitiesFlags;
import org.xyz.proxy.net.util.ByteReaderUtil;
import org.xyz.proxy.net.util.ByteWriterUtil;

import java.util.HashMap;
import java.util.Map;

@Data
public class HandshakeResponsePacket extends MySQLPacket {
    private static final byte[] FILLER_23 = new byte[23];

    public long clientFlag;
    public long maxPacketSize;
    public int charsetIndex;
    public String userName;

    public byte[] password;     // auth_response
    public String database;

    private String clientPluginName;

    private long clientConnectAttrsLength;
    private Map<String, String> clientConnectAttrs;

    public void read(BinaryPacket bin) {
        setPayloadLength(bin.getPayloadLength());
        setSequenceId(bin.getSequenceId());
        MySQLMessageStream mm = new MySQLMessageStream(bin.payload);
        clientFlag = mm.readUB4();
        maxPacketSize = mm.readUB4();
        charsetIndex = mm.readUB1();
        mm.inc(FILLER_23.length);  // filler
        userName = mm.readStringWithNull();

        int authResponseLength = 0;
        if((clientFlag & CapabilitiesFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) {
            // TODO: 这里存在一个BUG，authResponseLength可能会超过int的范围
            authResponseLength = (int) mm.readLengthEncodedInteger();
        } else {
            authResponseLength = mm.readUB1();
        }
        password = mm.readBytes(authResponseLength);

        if((clientFlag & CapabilitiesFlags.CLIENT_CONNECT_WITH_DB) == CapabilitiesFlags.CLIENT_CONNECT_WITH_DB) {
            database = mm.readStringWithNull();
        }

        if((clientFlag & CapabilitiesFlags.CLIENT_PLUGIN_AUTH) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH) {
            clientPluginName = mm.readStringWithNull();
        }

        if((clientFlag & CapabilitiesFlags.CLIENT_CONNECT_ATTRS) == CapabilitiesFlags.CLIENT_CONNECT_ATTRS) {
            clientConnectAttrs = new HashMap<>();
            clientConnectAttrsLength = mm.readLengthEncodedInteger();
            int readBytes = 0;
            while (readBytes < clientConnectAttrsLength) {
                // TODO: 这里存在一个BUG，keyLength可能会超过int的范围
                int keyLength = (int) mm.readLengthEncodedInteger();
                int keyLengthWidth = ByteReaderUtil.getLengthWidth(keyLength);
                String key = mm.readStringByLength(keyLength);
                int valueLength = (int) mm.readLengthEncodedInteger();
                int valueLengthWidth = ByteReaderUtil.getLengthWidth(valueLength);
                String value = mm.readStringByLength(valueLength);
                clientConnectAttrs.put(key, value);
                readBytes += keyLengthWidth + keyLength + valueLengthWidth + valueLength;
            }
        }
    }

    public void write(ChannelHandlerContext ctx) {
        // default init 256,so it can avoid buff extract
        ByteBuf buffer = ctx.alloc().buffer();
        ByteWriterUtil.writeUB3(buffer, calcPacketSize());
        ByteWriterUtil.writeUB1(buffer, getSequenceId());

        ByteWriterUtil.writeUB4(buffer, clientFlag);
        ByteWriterUtil.writeUB4(buffer, maxPacketSize);
        ByteWriterUtil.writeUB1(buffer, charsetIndex);
        ByteWriterUtil.writeBytes(buffer, FILLER_23);
        if (userName == null) {
            // 0x00
            ByteWriterUtil.writeUB1(buffer, 0);
        } else {
            ByteWriterUtil.writeBytesWithNull(buffer, userName.getBytes());
        }
        if (password == null) {
            // length
            ByteWriterUtil.writeUB1(buffer, 0);
        } else {
            ByteWriterUtil.writeBytesWithLength(buffer, password);
        }
        if ((clientFlag & CapabilitiesFlags.CLIENT_CONNECT_WITH_DB) == CapabilitiesFlags.CLIENT_CONNECT_WITH_DB) {
            ByteWriterUtil.writeBytesWithNull(buffer, database.getBytes());
        }
        if ((clientFlag & CapabilitiesFlags.CLIENT_PLUGIN_AUTH) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH) {
            ByteWriterUtil.writeBytesWithNull(buffer, clientPluginName.getBytes());
        }
        if ((clientFlag & CapabilitiesFlags.CLIENT_CONNECT_ATTRS) == CapabilitiesFlags.CLIENT_CONNECT_ATTRS) {
            ByteWriterUtil.writeLengthEncodedInteger(buffer, clientConnectAttrsLength);
            for(Map.Entry<String, String> kv : clientConnectAttrs.entrySet()) {
                String key = kv.getKey();
                String value = kv.getValue();

                ByteWriterUtil.writeBytesWithLength(buffer, key.getBytes());
                ByteWriterUtil.writeBytesWithLength(buffer, value.getBytes());
            }
        }
        ctx.writeAndFlush(buffer);
    }

    @Override
    public int calcPacketSize() {
        int size = 4;   // client_flag
        size += 4; // max_packet_size
        size += 1;// character_set
        size += FILLER_23.length;// filler
        size += userName.length() + 1;// username + null
        size += (password.length + ByteReaderUtil.getLengthWidth(password.length));// auth_response + auth_response length width
        if ((clientFlag & CapabilitiesFlags.CLIENT_CONNECT_WITH_DB) == CapabilitiesFlags.CLIENT_CONNECT_WITH_DB) {
            size += (database.length() + 1);// database + null
        }
        if ((clientFlag & CapabilitiesFlags.CLIENT_PLUGIN_AUTH) == CapabilitiesFlags.CLIENT_PLUGIN_AUTH) {
            size += (clientPluginName.length() + 1); // client_plugin_name + null
        }
        if ((clientFlag & CapabilitiesFlags.CLIENT_CONNECT_ATTRS) == CapabilitiesFlags.CLIENT_CONNECT_ATTRS) {
            size += (ByteReaderUtil.getLengthWidth(clientConnectAttrsLength) + clientConnectAttrsLength); // clientConnectAttrs length width + clientConnectAttrs length
        }
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL HandshakeResponsePacket Packet";
    }
}
