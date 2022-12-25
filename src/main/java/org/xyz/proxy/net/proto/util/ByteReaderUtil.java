package org.xyz.proxy.net.proto.util;

import io.netty.buffer.ByteBuf;
import org.xyz.proxy.net.proto.mysql.MySQLMessageStream;

public class ByteReaderUtil {
    /**
     * 一次读取一字节的数据（int<1>）
     * @param data Mysql协议数据包数据
     * @return 读取的数据
     * */
    public static int readUB1(ByteBuf data) {
        return data.readByte() & 0xFF;
    }

    /**
     * 一次读取两字节的数据（int<2>）
     * @param data Mysql协议数据包数据
     * @return 读取的数据
     * */
    public static int readUB2(ByteBuf data) {
        int i = data.readByte() & 0xFF;
        i |= (data.readByte() & 0xFF) << 8;
        return i;
    }

    /**
     * 一次读取三字节的数据（int<3>）
     * @param data Mysql协议数据包数据
     * @return 读取的数据
     * */
    public static int readUB3(ByteBuf data) {
        int i = data.readByte() & 0xFF;
        i |= (data.readByte() & 0xFF) << 8;
        i |= (data.readByte() & 0xFF) << 16;
        return i;
    }

    /**
     * 一次读取四字节的数据（int<4>）
     * @param data Mysql协议数据包数据
     * @return 读取的数据
     * */
    public static long readUB4(ByteBuf data) {
        long l = data.readByte() & 0xFF;
        l |= (data.readByte() & 0xFF) << 8;
        l |= (data.readByte() & 0xFF) << 16;
        l |= (long) (data.readByte() & 0xFF) << 24;
        return l;
    }

    /**
     * 一次读取六字节的数据（int<6>）
     * @param data Mysql协议数据包数据
     * @return 读取的数据
     * */
    public static long readUB6(ByteBuf data) {
        long l = data.readByte() & 0xFF;
        l |= (data.readByte() & 0xFF) << 8;
        l |= (data.readByte() & 0xFF) << 16;
        l |= (long) (data.readByte() & 0xFF) << 32;
        l |= (long) (data.readByte() & 0xFF) << 40;
        return l;
    }

    /**
     * 一次读取八字节的数据（int<8>）
     * @param data Mysql协议数据包数据
     * @return 读取的数据
     * */
    public static long readUB8(ByteBuf data) {
        long l = (data.readByte() & 0xFF);
        l |= (long) (data.readByte() & 0xFF) << 8;
        l |= (long) (data.readByte() & 0xFF) << 16;
        l |= (long) (data.readByte() & 0xFF) << 24;
        l |= (long) (data.readByte() & 0xFF) << 32;
        l |= (long) (data.readByte() & 0xFF) << 40;
        l |= (long) (data.readByte() & 0xFF) << 48;
        l |= (long) (data.readByte() & 0xFF) << 56;
        return l;
    }

    /**
     * 读取 Length-Encoded Integer 类型数据
     * @param data Mysql协议数据包数据
     * @return 读取的数据
     */
    public static long readLengthEncodedInteger(ByteBuf data) {
        int length = data.readByte() & 0xFF;
        switch (length) {
            case 0XFB:  // 251，无效数据
                return MySQLMessageStream.NULL_LENGTH_ENCODED_INTEGER;
            case 0xFC:  // 0xFC + 2-byte integer
                return readUB2(data);
            case 0xFD:   // 0xFD + 3-byte integer
                return readUB3(data);
            case 0xFE:   // 0xFE + 8-byte integer
                return readUB8(data);
            default:    // 1-byte integer
                return length;
        }
    }

    /**
     * 编码 Length-Encoded Integer 类型数据
     * @param length 长度值
     * @return 编码后的长度值
     */
    public static byte[] encodedLengthInteger(long length) {
        byte[] lengthByte = null;
        if (length < 251) {
            lengthByte = new byte[1];
            lengthByte[0] = (byte) (length & 0xFF);
        } else if (length < 0x10000L) {
            lengthByte = new byte[3];
            lengthByte[0] = (byte) 0xFC;
            lengthByte[1] = (byte) (length & 0xFF);
            lengthByte[2] = (byte) ((length >> 8) & 0xFF);
        } else if (length < 0x1000000L) {
            lengthByte = new byte[4];
            lengthByte[0] = (byte) 0xFD;
            lengthByte[1] = (byte) (length & 0xFF);
            lengthByte[2] = (byte) ((length >> 8) & 0xFF);
            lengthByte[3] = (byte) ((length >> 16) & 0xFF);
        } else {
            lengthByte = new byte[9];
            lengthByte[0] = (byte) 0xFE;
            lengthByte[1] = (byte) (length & 0xFF);
            lengthByte[2] = (byte) ((length >> 8) & 0xFF);
            lengthByte[3] = (byte) ((length >> 16) & 0xFF);
            lengthByte[4] = (byte) ((length >> 24) & 0xFF);
            lengthByte[5] = (byte) ((length >> 32) & 0xFF);
            lengthByte[6] = (byte) ((length >> 40) & 0xFF);
            lengthByte[7] = (byte) ((length >> 48) & 0xFF);
            lengthByte[8] = (byte) ((length >> 56) & 0xFF);
        }
        return lengthByte;
    }

    public static void main(String[] args) {
        byte[] bytes = encodedLengthInteger((long) Math.pow(2, 16));
        for(byte b : bytes) {
            System.out.println(Integer.toHexString(b));
        }
    }
}
