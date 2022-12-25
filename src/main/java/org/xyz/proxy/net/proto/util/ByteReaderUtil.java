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
    public static int readUB4(ByteBuf data) {
        int i = data.readByte() & 0xFF;
        i |= (data.readByte() & 0xFF) << 8;
        i |= (data.readByte() & 0xFF) << 16;
        i |= (long) (data.readByte() & 0xFF) << 24;
        return i;
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
     * 获取 Length-Encoded Integer 类型数据的宽度
     * @param length Length-Encoded Integer 类型数据
     * @return Length-Encoded Integer 类型数据的宽度
     */
    public static final int getLengthWidth(long length) {
        if (length < 251) {
            return 1;
        } else if (length < 0x10000L) {
            return 3;
        } else if (length < 0x1000000L) {
            return 4;
        } else {
            return 9;
        }
    }

    /**
     * 获取 LengthEncodedString 类型数据长度的宽度（包含长度值的宽度+String数据宽度）
     * @param src LengthEncodedString 类型数据
     * @return LengthEncodedString 类型数据长度的宽度（包含长度值的宽度+String数据宽度）
     */
    public static final int getLengthWidth(byte[] src) {
        int length = src.length;
        if (length < 251) {
            return 1 + length;
        } else if (length < 0x10000L) {
            return 3 + length;
        } else if (length < 0x1000000L) {
            return 4 + length;
        } else {
            return 9 + length;
        }
    }
}
