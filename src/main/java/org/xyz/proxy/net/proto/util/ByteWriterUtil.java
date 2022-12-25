package org.xyz.proxy.net.proto.util;

import io.netty.buffer.ByteBuf;

public class ByteWriterUtil {
    /**
     * 一次写入一字节的数据（int<1>），无符号整型
     * @param buffer 需要写入的byteBuf
     * @param i 需要写入的数据
     * */
    public static void writeUB1(ByteBuf buffer, int i) {
        buffer.writeByte((byte) (i & 0xff));
    }

    /**
     * 一次写入二字节的数据（int<2>），无符号整型
     * @param buffer 需要写入的byteBuf
     * @param i 需要写入的数据
     * */
    public static void writeUB2(ByteBuf buffer, int i) {
        buffer.writeByte((byte) (i & 0xff));
        buffer.writeByte((byte) (i >>> 8));
    }

    /**
     * 一次写入三字节的数据（int<3>），无符号整型
     * @param buffer 需要写入的byteBuf
     * @param i 需要写入的数据
     * */
    public static void writeUB3(ByteBuf buffer, int i) {
        buffer.writeByte((byte) (i & 0xff));
        buffer.writeByte((byte) (i >>> 8));
        buffer.writeByte((byte) (i >>> 16));
    }

    /**
     * 一次写入四字节的数据（int<4>），无符号整型
     * @param buffer 需要写入的byteBuf
     * @param l 需要写入的数据
     * */
    public static void writeUB4(ByteBuf buffer, long l) {
        buffer.writeByte((byte) (l & 0xff));
        buffer.writeByte((byte) (l >>> 8));
        buffer.writeByte((byte) (l >>> 16));
        buffer.writeByte((byte) (l >>> 24));
    }

    /**
     * 一次写入四字节的数据（int<4>），有符号整型
     * @param buffer 需要写入的byteBuf
     * @param l 需要写入的数据
     * */
    public static void writeInt(ByteBuf buffer, int l) {
        buffer.writeByte((byte) (l & 0xff));
        buffer.writeByte((byte) (l >>> 8));
        buffer.writeByte((byte) (l >>> 16));
        buffer.writeByte((byte) (l >>> 24));
    }


    /**
     * 一次写入六字节的数据（int<6>），无符号整型
     * @param buffer 需要写入的byteBuf
     * @param l 需要写入的数据
     * */
    public static void writeUB6(ByteBuf buffer, long l) {
        buffer.writeByte((byte) (l & 0xff));
        buffer.writeByte((byte) (l >>> 8));
        buffer.writeByte((byte) (l >>> 16));
        buffer.writeByte((byte) (l >>> 24));
        buffer.writeByte((byte) (l >>> 32));
        buffer.writeByte((byte) (l >>> 40));
    }

    /**
     * 一次写入八字节的数据（int<8>），有符号整型
     * @param buffer 需要写入的byteBuf
     * @param l 需要写入的数据
     * */
    public static void writeLong(ByteBuf buffer, long l) {
        buffer.writeByte((byte) (l & 0xff));
        buffer.writeByte((byte) (l >>> 8));
        buffer.writeByte((byte) (l >>> 16));
        buffer.writeByte((byte) (l >>> 24));
        buffer.writeByte((byte) (l >>> 32));
        buffer.writeByte((byte) (l >>> 40));
        buffer.writeByte((byte) (l >>> 48));
        buffer.writeByte((byte) (l >>> 56));
    }

    /**
     * 写入 Length-Encoded Integer 类型数据
     * @param buffer 需要写入的byteBuf
     * @param l 需要写入的数据
     */
    public static void writeLengthEncodedInteger(ByteBuf buffer, long l) {
        if (l < 251) {
            buffer.writeByte((byte) l);
        } else if (l < 0x10000L) {
            buffer.writeByte((byte) 0xFC);
            writeUB2(buffer, (int) l);
        } else if (l < 0x1000000L) {
            buffer.writeByte((byte) 0xFD);
            writeUB3(buffer, (int) l);
        } else {
            buffer.writeByte((byte) 0xFE);
            writeLong(buffer, l);
        }
    }

    /**
     * 写入 NullTerminatedString 类型数据
     * @param buffer 需要写入的byteBuf
     * @param src 需要写入的数据
     */
    public static void writeStringWithNull(ByteBuf buffer, byte[] src) {
        buffer.writeBytes(src);
        buffer.writeByte((byte) 0);
    }

    /**
     * 写入 LengthEncodedString 类型数据
     * @param buffer 需要写入的byteBuf
     * @param src 需要写入的数据
     */
    public static void writeStringWithLength(ByteBuf buffer, byte[] src) {
        int length = src.length;
        if (length < 251) {
            buffer.writeByte((byte) length);
        } else if (length < 0x10000L) {
            buffer.writeByte((byte) 0xFC);
            writeUB2(buffer, length);
        } else if (length < 0x1000000L) {
            buffer.writeByte((byte) 0xFD);
            writeUB3(buffer, length);
        } else {
            buffer.writeByte((byte) 0xFE);
            writeLong(buffer, length);
        }
        buffer.writeBytes(src);
    }

    /**
     * 写入 LengthEncodedString 类型数据，如果写入数据为null，则写入nullValue
     * @param buffer 需要写入的byteBuf
     * @param nullValue 当写入数据src为null时，需要写入的数据
     * @param src 需要写入的数据
     */
    public static final void writeStringWithLength(ByteBuf buffer, byte[] src, byte nullValue) {
        if (src == null) {
            buffer.writeByte(nullValue);
        } else {
            writeStringWithLength(buffer, src);
        }
    }
}
