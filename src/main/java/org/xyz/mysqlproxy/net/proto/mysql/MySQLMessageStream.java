package org.xyz.mysqlproxy.net.proto.mysql;

import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

// MySQL消息流
public class MySQLMessageStream {
    public static final long NULL_LENGTH_ENCODED_INTEGER = -1;
    public static final byte[] EMPTY_BYTES = new byte[0];

    // 存储mysql消息流的字节数组
    private final ByteBuf data;
    // mysql消息流的长度(Byte，存储mysql消息流的字节数组的大小)
    private final int length;
    // mysql消息流的当前读取索引

    public MySQLMessageStream(ByteBuf data) {
        this.data = data;
        this.length = data.readableBytes();
    }

    public int length() {
        return length;
    }

    /**
     * 获取当前字节流读取索引
     * @return 当前字节流读取索引的值
     * */
    public int current() {
        return data.readerIndex();
    }

    public ByteBuf data() {
        return data;
    }

    /**
     * 向后移动字节流读取索引
     * @param step 向后移动的步长
     * */
    public void inc(int step) {
        data.readerIndex(data.readerIndex() + step);
    }

    /**
     * 设置字节流读取索引
     * @param position 字节流的读取索引
     * */
    public void setPosition(int position) {
        data.readerIndex(position);
    }

    /**
     * 字节流中是否还有数据未读完
     * @return 字节流中是否还有数据未读完
     * */
    public boolean hasRemaining() {
        return data.readableBytes() > 0;
    }

    /**
     * 一次读取一字节的数据（int<1>），有符号整型，不增加索引
     * @return 读取的数据
     * */
    public byte read(int i) {
        return data.getByte(i);
    }

    /**
     * 一次读取一字节的数据（int<1>），有符号整型
     * @return 读取的数据
     * */
    public byte readByte() {
        return data.readByte();
    }

    /**
     * 一次读取一字节的数据（int<1>），无符号整型
     * @return 读取的数据
     * */
    public int readUB1() {
        return data.readByte() & 0xFF;
    }

    /**
     * 一次读取两字节的数据（int<2>），无符号整型
     * @return 读取的数据
     * */
    public int readUB2() {
        int i = data.readByte() & 0xff;
        i |= (data.readByte() & 0xff) << 8;
        return i;
    }

    /**
     * 一次读取三字节的数据（int<3>），无符号整型
     * @return 读取的数据
     * */
    public int readUB3() {
        int i = data.readByte() & 0xff;
        i |= (data.readByte() & 0xff) << 8;
        i |= (data.readByte() & 0xff) << 16;
        return i;
    }

    /**
     * 一次读取四字节的数据（int<4>），无符号整型
     * @return 读取的数据
     * */
    public long readUB4() {
        long l = (long) (data.readByte() & 0xff);
        l |= (long) (data.readByte() & 0xff) << 8;
        l |= (long) (data.readByte() & 0xff) << 16;
        l |= (long) (data.readByte() & 0xff) << 24;
        return l;
    }

    /**
     * 一次读取四字节的数据（int<4>），有符号整型
     * @return 读取的数据
     * */
    public int readInt() {
        int i = data.readByte() & 0xff;
        i |= (data.readByte() & 0xff) << 8;
        i |= (data.readByte() & 0xff) << 16;
        i |= (data.readByte() & 0xff) << 24;
        return i;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * 一次读取六字节的数据（int<6>），无符号整型
     * @return 读取的数据
     * */
    public long readUB6() {
        long l = (long) (data.readByte() & 0xff);
        l |= (long) (data.readByte() & 0xff) << 8;
        l |= (long) (data.readByte() & 0xff) << 16;
        l |= (long) (data.readByte() & 0xff) << 24;
        l |= (long) (data.readByte() & 0xff) << 32;
        l |= (long) (data.readByte() & 0xff) << 40;
        return l;
    }

    /**
     * 一次读取八字节的数据（int<8>），有符号整型
     * @return 读取的数据
     * */
    public long readLong() {
        long l = (long) (data.readByte() & 0xff);
        l |= (long) (data.readByte() & 0xff) << 8;
        l |= (long) (data.readByte() & 0xff) << 16;
        l |= (long) (data.readByte() & 0xff) << 24;
        l |= (long) (data.readByte() & 0xff) << 32;
        l |= (long) (data.readByte() & 0xff) << 40;
        l |= (long) (data.readByte() & 0xff) << 48;
        l |= (long) (data.readByte() & 0xff) << 56;
        return l;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * 读取 Length-Encoded Integer 类型数据
     * @return 读取的数据
     */
    public long readLengthEncodedInteger() {
        int length = data.readByte() & 0xff;
        switch (length) {
            case 0XFB:  // 251，无效数据
                return MySQLMessageStream.NULL_LENGTH_ENCODED_INTEGER;
            case 0xFC:  // 0xFC + 2-byte integer
                return readUB2();
            case 0xFD:   // 0xFD + 3-byte integer
                return readUB3();
            case 0xFE:   // 0xFE + 8-byte integer
                // TODO: 这里存在一个BUG，因为JAVA中没有无符号Long，所以这里读取的length可能是负值
                return readLong();
            default:    // 1-byte integer
                return length;
        }
    }

    /**
     * 读取所有剩余未读字节string<EOF>
     * @return 读取的数据
     */
    public byte[] readBytes() {
        if (!hasRemaining()) {
            return EMPTY_BYTES;
        }
        int position = current();
        byte[] ab = new byte[length - position];
        data.getBytes(position, ab);
        position = length;
        data.readerIndex(position);
        return ab;
    }

    /**
     * 读取length长度的剩余未读字节
     * @param length 读取的字节数
     * @return 读取的数据
     */
    public byte[] readBytes(int length) {
        int position = current();
        byte[] ab = new byte[length];
        data.getBytes(position, ab);
        position += length;
        data.readerIndex(position);
        return ab;
    }

    /**
     * 读取以NUL结束的字符串（string<NUL>）
     * @return 读取的数据
     */
    public byte[] readBytesWithNull() {
        if (!hasRemaining()) {
            return EMPTY_BYTES;
        }
        int position = current();
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (data.getByte(i) == 0) {
                offset = i;
                break;
            }
        }
        if(offset == -1) {  // 读到结尾也没有读到0x00，不合法的字符串，返回null
            return null;
        } else if (offset == position) {    // 空字符串
            position++;
            data.readerIndex(position);
            return EMPTY_BYTES;
        } else {    // 以0x00结尾的字符串
            byte[] ab2 = new byte[offset - position];
            data.getBytes(position, ab2);
            position = offset + 1;
            data.readerIndex(position);
            return ab2;
        }
    }

    /**
     * 读取长度编码的字符串（LengthEncodedString）
     * @return 读取的数据
     */
    public byte[] readBytesWithLength() {
        // TODO: 这里存在一个BUG，length可能会超过int的范围
        int length = (int) readLengthEncodedInteger();
        if (length <= 0) {
            return EMPTY_BYTES;
        }
        int position = current();
        byte[] ab = new byte[length];
        data.getBytes(position, ab);
        position += length;
        data.readerIndex(position);
        return ab;
    }

    /**
     * 读取EOF结束的字符串，数据包最后一部分的字符串，长度为包长减去当前位置（RestOfPacketString）
     * @return 读取的字符串String对象
     */
    public String readString() {
        if (!hasRemaining()) {
            return null;
        }
        int position = current();
        byte[] ab = new byte[length - position];
        data.getBytes(position, ab);
        String s = new String(ab);
        position = length;
        data.readerIndex(position);
        return s;
    }

    /**
     * 读取EOF结束的字符串，数据包最后一部分的字符串，长度为包长减去当前位置（RestOfPacketString），然后以charset编码格式返回String对象
     * @param charset 编码
     * @return 读取的字符串String对象
     */
    public String readString(String charset) throws UnsupportedEncodingException {
        if (!hasRemaining()) {
            return null;
        }
        int position = current();
        byte[] ab = new byte[length - position];
        data.getBytes(position, ab);
        String s = new String(ab);
        position = length;
        data.readerIndex(position);
        return s;
    }

    /**
     * 读取以NUL结束的字符串（string<NUL>）
     * @return 读取的字符串String对象
     */
    public String readStringWithNull() {
        if (!hasRemaining()) {
            return null;
        }
        int position = current();
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (data.getByte(i) == 0) {
                offset = i;
                break;
            }
        }
        if (offset == -1) { // 读到结尾也没有读到0x00，不合法的字符串，返回null
            return null;
        } else if (offset == position) {    // 空字符串
            position++;
            data.readerIndex(position);
            return null;
        } else {
            byte[] ab = new byte[offset - position];
            data.getBytes(position, ab);
            String s = new String(ab);
            position = offset + 1;
            data.readerIndex(position);
            return s;
        }
    }

    /**
     * 读取以NUL结束的字符串（string<NUL>），然后以charset编码格式返回String对象
     * @param charset 编码
     * @return 读取的字符串String对象
     */
    public String readStringWithNull(String charset) throws UnsupportedEncodingException {
        if (!hasRemaining()) {
            return null;
        }
        int position = current();
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (data.getByte(i) == 0) {
                offset = i;
                break;
            }
        }
        if (offset == -1) { // 读到结尾也没有读到0x00，不合法的字符串，返回null
            return null;
        } else if (offset == position) {    // 空字符串
            position++;
            data.readerIndex(position);
            return null;
        } else {
            byte[] ab = new byte[offset - position];
            data.getBytes(position, ab);
            String s = new String(ab, charset);
            position = offset + 1;
            data.readerIndex(position);
            return s;
        }
    }

    /**
     * 读取长度编码的字符串（LengthEncodedString）
     * @param length 读取的字符长度
     * @return 读取的字符串String对象
     */
    public String readStringByLength(int length) {
        if (length <= 0) {
            return null;
        }
        int position = current();
        byte[] ab = new byte[length];
        data.getBytes(position, ab);
        String s = new String(ab);
        position += length;
        data.readerIndex(position);
        return s;
    }

    /**
     * 读取长度编码的字符串（LengthEncodedString）
     * @return 读取的字符串String对象
     */
    public String readStringWithLength() {
        // TODO: 这里存在一个BUG，length可能会超过int的范围
        int length = (int) readLengthEncodedInteger();
        if (length <= 0) {
            return null;
        }
        int position = current();
        byte[] ab = new byte[length];
        data.getBytes(position, ab);
        String s = new String(ab);
        position += length;
        data.readerIndex(position);
        return s;
    }

    /**
     * 读取长度编码的字符串（LengthEncodedString）
     * @param charset 编码
     * @return 读取的字符串String对象，然后以charset编码格式返回String对象
     */
    public String readStringWithLength(String charset) throws UnsupportedEncodingException {
        // TODO: 这里存在一个BUG，length可能会超过int的范围
        int length = (int) readLengthEncodedInteger();
        if (length <= 0) {
            return null;
        }
        int position = current();
        byte[] ab = new byte[length];
        data.getBytes(position, ab);
        String s = new String(ab, charset);
        position += length;
        data.readerIndex(position);
        return s;
    }

    /**
     * 读取时间
     * @return 读取的数据
     */
    public java.sql.Time readTime() {
        inc(6);
        int hour = readUB1();
        int minute = readUB1();
        int second = readUB1();
        Calendar cal = getLocalCalendar();
        cal.set(0, 0, 0, hour, minute, second);
        return new Time(cal.getTimeInMillis());
    }

    /**
     * 读取日期
     * @return 读取的数据
     */
    public java.util.Date readDate() {
        int length = readUB1();
        int year = readUB2();
        int month = readUB1();
        int date = readUB1();
        int hour = readUB1();
        int minute = readUB1();
        int second = readUB1();
        if (length == 11) {
            long nanos = readUB4();
            Calendar cal = getLocalCalendar();
            cal.set(year, --month, date, hour, minute, second);
            Timestamp time = new Timestamp(cal.getTimeInMillis());
            time.setNanos((int) nanos);
            return time;
        } else {
            Calendar cal = getLocalCalendar();
            cal.set(year, --month, date, hour, minute, second);
            return new java.sql.Date(cal.getTimeInMillis());
        }
    }

    /**
     * 读取大数
     * @return 读取的数据
     */
    public BigDecimal readBigDecimal() {
        String src = readString();
        return src == null ? null : new BigDecimal(src);
    }

    // 单例模式+ThreadLocal的Calendar实例
    private static final ThreadLocal<Calendar> localCalendar = new ThreadLocal<Calendar>();

    private static final Calendar getLocalCalendar() {
        Calendar cal = localCalendar.get();
        if (cal == null) {
            cal = Calendar.getInstance();
            localCalendar.set(cal);
        }
        return cal;
    }
}
