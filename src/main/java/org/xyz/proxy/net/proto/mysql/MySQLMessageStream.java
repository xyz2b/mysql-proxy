package org.xyz.proxy.net.proto.mysql;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

// MySQL消息流
public class MySQLMessageStream {
    public static final long NULL_LENGTH_ENCODED_INTEGER = -1;
    public static final byte[] EMPTY_BYTES = new byte[0];

    // 存储mysql消息流的字节数组
    private final byte[] data;
    // mysql消息流的长度(Byte，存储mysql消息流的字节数组的大小)
    private final int length;
    // mysql消息流的当前读取索引
    private int position;

    public MySQLMessageStream(byte[] data) {
        this.data = data;
        this.length = data.length;
        this.position = 0;
    }

    public int length() {
        return length;
    }

    public int current() {
        return position;
    }

    public byte[] data() {
        return data;
    }

    public void inc(int i) {
        position += i;
    }

    public void setPosition(int i) {
        this.position = i;
    }

    public boolean hasRemaining() {
        return length > position;
    }

    public byte read(int i) {
        return data[i];
    }

    public byte readUB1() {
        return data[position++];
    }

    public int readUB2() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        return i;
    }

    public int readUB3() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        return i;
    }

    public long readUB4() {
        final byte[] b = this.data;
        long l = (long) (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        return l;
    }

    public int readInt() {
        final byte[] b = this.data;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        i |= (b[position++] & 0xff) << 24;
        return i;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public long readLong() {
        final byte[] b = this.data;
        long l = (long) (b[position++] & 0xff);
        l |= (long) (b[position++] & 0xff) << 8;
        l |= (long) (b[position++] & 0xff) << 16;
        l |= (long) (b[position++] & 0xff) << 24;
        l |= (long) (b[position++] & 0xff) << 32;
        l |= (long) (b[position++] & 0xff) << 40;
        l |= (long) (b[position++] & 0xff) << 48;
        l |= (long) (b[position++] & 0xff) << 56;
        return l;
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * 读取 Length-Encoded Integer 类型数据
     * @return 读取的数据
     */
    public long readLength() {
        int length = data[position++] & 0xff;
        switch (length) {
            case 0XFB:  // 251，无效数据
                return MySQLMessageStream.NULL_LENGTH_ENCODED_INTEGER;
            case 0xFC:  // 0xFC + 2-byte integer
                return readUB2();
            case 0xFD:   // 0xFD + 3-byte integer
                return readUB3();
            case 0xFE:   // 0xFE + 8-byte integer
                return readLong();
            default:    // 1-byte integer
                return length;
        }
    }

    /**
     * 读取所有剩余未读字节
     * @return 读取的数据
     */
    public byte[] readBytes() {
        if (position >= length) {
            return EMPTY_BYTES;
        }
        byte[] ab = new byte[length - position];
        System.arraycopy(data, position, ab, 0, ab.length);
        position = length;
        return ab;
    }

    /**
     * 读取length长度的剩余未读字节
     * @return 读取的数据
     */
    public byte[] readBytes(int length) {
        byte[] ab = new byte[length];
        System.arraycopy(data, position, ab, 0, length);
        position += length;
        return ab;
    }

    /**
     * 读取以NUL结束的字符串（string<NUL>）
     * @return 读取的数据
     */
    public byte[] readBytesWithNull() {
        final byte[] b = this.data;
        if (position >= length) {
            return EMPTY_BYTES;
        }
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        if(offset == -1) {  // 读到结尾也没有读到0x00，不合法的字符串，返回null
            return null;
        } else if (offset == position) {    // 空字符串
            position++;
            return EMPTY_BYTES;
        } else {    // 以0x00结尾的字符串
            byte[] ab2 = new byte[offset - position];
            System.arraycopy(b, position, ab2, 0, ab2.length);
            position = offset + 1;
            return ab2;
        }
    }

    /**
     * 读取长度编码的字符串（LengthEncodedString）
     * @return 读取的数据
     */
    public byte[] readBytesWithLength() {
        int length = (int) readLength();
        if (length <= 0) {
            return EMPTY_BYTES;
        }
        byte[] ab = new byte[length];
        System.arraycopy(data, position, ab, 0, ab.length);
        position += length;
        return ab;
    }

    /**
     * 读取EOF结束的字符串，数据包最后一部分的字符串，长度为包长减去当前位置（RestOfPacketString）
     * @return 读取的字符串String对象
     */
    public String readString() {
        if (position >= length) {
            return null;
        }
        String s = new String(data, position, length - position);
        position = length;
        return s;
    }

    /**
     * 读取EOF结束的字符串，数据包最后一部分的字符串，长度为包长减去当前位置（RestOfPacketString），然后以charset编码格式返回String对象
     * @return 读取的字符串String对象
     */
    public String readString(String charset) throws UnsupportedEncodingException {
        if (position >= length) {
            return null;
        }
        String s = new String(data, position, length - position, charset);
        position = length;
        return s;
    }

    /**
     * 读取以NUL结束的字符串（string<NUL>）
     * @return 读取的字符串String对象
     */
    public String readStringWithNull() {
        final byte[] b = this.data;
        if (position >= length) {
            return null;
        }
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        if (offset == -1) { // 读到结尾也没有读到0x00，不合法的字符串，返回null
            return null;
        } else if (offset == position) {    // 空字符串
            position++;
            return null;
        } else {
            String s = new String(b, position, offset - position);
            position = offset + 1;
            return s;
        }
    }

    /**
     * 读取以NUL结束的字符串（string<NUL>），然后以charset编码格式返回String对象
     * @return 读取的字符串String对象
     */
    public String readStringWithNull(String charset) throws UnsupportedEncodingException {
        final byte[] b = this.data;
        if (position >= length) {
            return null;
        }
        int offset = -1;
        for (int i = position; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
            case -1:
                String s1 = new String(b, position, length - position, charset);
                position = length;
                return s1;
            case 0:
                position++;
                return null;
            default:
                String s2 = new String(b, position, offset - position, charset);
                position = offset + 1;
                return s2;
        }
    }

    /**
     * 读取长度编码的字符串（LengthEncodedString）
     * @return 读取的字符串String对象
     */
    public String readStringWithLength() {
        int length = (int) readLength();
        if (length <= 0) {
            return null;
        }
        String s = new String(data, position, length);
        position += length;
        return s;
    }

    /**
     * 读取长度编码的字符串（LengthEncodedString）
     * @return 读取的字符串String对象，然后以charset编码格式返回String对象
     */
    public String readStringWithLength(String charset) throws UnsupportedEncodingException {
        int length = (int) readLength();
        if (length <= 0) {
            return null;
        }
        String s = new String(data, position, length, charset);
        position += length;
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
        byte length = readUB1();
        int year = readUB2();
        byte month = readUB1();
        byte date = readUB1();
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
        String src = readStringWithLength();
        return src == null ? null : new BigDecimal(src);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(Arrays.toString(data)).toString();
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
