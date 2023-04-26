package org.xyz.proxy.db.protocol.mysql.payload;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.proxy.db.protocol.core.payload.PacketPayload;
import java.nio.charset.Charset;

/**
 * MySQL payload operation for MySQL packet data types.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_packets.html#sect_protocol_basic_packets_packet">describing packets</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLPacketPayload implements PacketPayload {
    private final ByteBuf byteBuf;

    private final Charset charset;

    /**
     * Read 1 byte fixed length integer from byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @return 1 byte fixed length integer
     */
    public int readInt1() {
        return byteBuf.readUnsignedByte();
    }

    /**
     * Write 1 byte fixed length integer to byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @param value 1 byte fixed length integer
     */
    public void writeInt1(final int value) {
        byteBuf.writeByte(value);
    }

    /**
     * Read 2 byte fixed length integer from byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @return 2 byte fixed length integer
     */
    public int readInt2() {
        return byteBuf.readUnsignedShortLE();
    }

    /**
     * Write 2 byte fixed length integer to byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @param value 2 byte fixed length integer
     */
    public void writeInt2(final int value) {
        byteBuf.writeShortLE(value);
    }

    /**
     * Read 3 byte fixed length integer from byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @return 3 byte fixed length integer
     */
    public int readInt3() {
        return byteBuf.readUnsignedMediumLE();
    }

    /**
     * Write 3 byte fixed length integer to byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @param value 3 byte fixed length integer
     */
    public void writeInt3(final int value) {
        byteBuf.writeMediumLE(value);
    }

    /**
     * Read 4 byte fixed length integer from byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @return 4 byte fixed length integer
     */
    public int readInt4() {
        return byteBuf.readIntLE();
    }

    /**
     * Write 4 byte fixed length integer to byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @param value 4 byte fixed length integer
     */
    public void writeInt4(final int value) {
        byteBuf.writeIntLE(value);
    }

    /**
     * Read 6 byte fixed length integer from byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @return 6 byte fixed length integer
     */
    public long readInt6() {
        long result = 0;
        for (int i = 0; i < 6; i++) {
            result |= ((long) (0xff & byteBuf.readByte())) << (8 * i);
        }
        return result;
    }

    /**
     * Write 6 byte fixed length integer to byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @param value 6 byte fixed length integer
     */
    public void writeInt6(final long value) {
        for (int i = 0; i < 6; i++) {
            byteBuf.writeByte((int) ((value >> (8 * i)) & 0xff));
        }
    }

    /**
     * Read 8 byte fixed length integer from byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @return 8 byte fixed length integer
     */
    public long readInt8() {
        return byteBuf.readLongLE();
    }

    /**
     * Write 8 byte fixed length integer to byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_fixed">FixedLengthInteger</a>
     *
     * @param value 8 byte fixed length integer
     */
    public void writeInt8(final long value) {
        byteBuf.writeLongLE(value);
    }

    /**
     * Read lenenc integer from byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_le">LengthEncodedInteger</a>
     *
     * @return lenenc integer
     */
    public long readIntLenenc() {
        int firstByte = readInt1();
        // < 251
        if (firstByte < 0xfb) {
            return firstByte;
        }
        // invalid
        if (0xfb == firstByte) {
            return 0;
        }
        // 0xFC + 2-byte integer
        if (0xfc == firstByte) {
            return readInt2();
        }
        // 0xFD + 3-byte integer
        if (0xfd == firstByte) {
            return readInt3();
        }
        // 0xFE + 8-byte integer
        return byteBuf.readLongLE();
    }

    /**
     * Write lenenc integer to byte buffers (Little Endian Byte Order).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_integers.html#sect_protocol_basic_dt_int_le">LengthEncodedInteger</a>
     *
     * @param value lenenc integer
     */
    public void writeIntLenenc(final long value) {
        // 0 <= value < 251
        if (value < 0xfb) {
            byteBuf.writeByte((int) value);
            return;
        }
        // 251 <= value < 2^16
        if (value < Math.pow(2, 16)) {
            byteBuf.writeByte(0xfc);
            byteBuf.writeShortLE((int) value);
            return;
        }
        // 2^16 <= value < 2^24
        if (value < Math.pow(2, 24)) {
            byteBuf.writeByte(0xfd);
            byteBuf.writeMediumLE((int) value);
            return;
        }
        // 2^24 <= value < 2^64
        byteBuf.writeByte(0xfe);
        byteBuf.writeLongLE(value);
    }

    /**
     * Read fixed length long from byte buffers.
     *
     * @param length length read from byte buffers
     *
     * @return fixed length long
     */
    public long readLong(final int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            result = result << 8 | readInt1();
        }
        return result;
    }

    /**
     * Read lenenc string from byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_le">LengthEncodedString</a>
     *
     * @return lenenc string
     */
    public String readStringLenenc() {
        int length = (int) readIntLenenc();
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return new String(result, charset);
    }

    /**
     * Read lenenc string from byte buffers for bytes.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_le">LengthEncodedString</a>
     *
     * @return lenenc bytes
     */
    public byte[] readStringLenencByBytes() {
        int length = (int) readIntLenenc();
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }

    /**
     * Write lenenc string to byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_le">LengthEncodedString</a>
     *
     * @param value lenenc string
     */
    public void writeStringLenenc(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            byteBuf.writeByte(0);
            return;
        }
        byte[] valueBytes = value.getBytes(charset);
        writeIntLenenc(valueBytes.length);
        byteBuf.writeBytes(valueBytes);
    }

    /**
     * Write lenenc bytes to byte buffers.
     *
     * @param value lenenc bytes
     */
    public void writeBytesLenenc(final byte[] value) {
        if (0 == value.length) {
            byteBuf.writeByte(0);
            return;
        }
        writeIntLenenc(value.length);
        byteBuf.writeBytes(value);
    }

    /**
     * Read fixed length string from byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_fix">FixedLengthString</a>
     *
     * @param length length of fixed string
     *
     * @return fixed length string
     */
    public String readStringFix(final int length) {
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return new String(result, charset);
    }

    /**
     * Read fixed length string from byte buffers and return bytes.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_fix">FixedLengthString</a>
     *
     * @param length length of fixed string
     *
     * @return fixed length bytes
     */
    public byte[] readStringFixByBytes(final int length) {
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }

    /**
     * Write fixed length string to byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_fix">FixedLengthString</a>
     *
     * @param value fixed length string
     */
    public void writeStringFix(final String value) {
        byteBuf.writeBytes(value.getBytes(charset));
    }

    /**
     * Write fixed length bytes to byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_fix">FixedLengthString</a>
     *
     * @param value fixed length bytes
     */
    public void writeBytes(final byte[] value) {
        byteBuf.writeBytes(value);
    }

    /**
     * Read variable length string from byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_var">VariableLengthString</a>
     *
     * @return variable length string
     */
    public String readStringVar() {
        // TODO
        return "";
    }

    /**
     * Write fixed length string to byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_var">VariableLengthString</a>
     *
     * @param value variable length string
     */
    public void writeStringVar(final String value) {
        // TODO
    }

    /**
     * Read null terminated string from byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_null">NulTerminatedString</a>
     *
     * @return null terminated string
     */
    public String readStringNul() {
        byte[] result = new byte[byteBuf.bytesBefore((byte) 0)];
        byteBuf.readBytes(result);
        byteBuf.skipBytes(1);
        return new String(result, charset);
    }

    /**
     * Read null terminated string from byte buffers and return bytes.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_null">NulTerminatedString</a>
     *
     * @return null terminated bytes
     */
    public byte[] readStringNulByBytes() {
        byte[] result = new byte[byteBuf.bytesBefore((byte) 0)];
        byteBuf.readBytes(result);
        byteBuf.skipBytes(1);
        return result;
    }

    /**
     * Write null terminated string to byte buffers.
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_null">NulTerminatedString</a>
     *
     * @param value null terminated string
     */
    public void writeStringNul(final String value) {
        byteBuf.writeBytes(value.getBytes(charset));
        byteBuf.writeByte(0);
    }

    /**
     * Read rest of packet string from byte buffers and return bytes (the last component of a packet).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_eof">RestOfPacketString</a>
     *
     * @return rest of packet string bytes
     */
    public byte[] readStringEOFByBytes() {
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return result;
    }

    /**
     * Read rest of packet string from byte buffers (the last component of a packet).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_eof">RestOfPacketString</a>
     *
     * @return rest of packet string
     */
    public String readStringEOF() {
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return new String(result, charset);
    }

    /**
     * Write rest of packet string to byte buffers (the last component of a packet).
     *
     * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_dt_strings.html#sect_protocol_basic_dt_string_eof">RestOfPacketString</a>
     *
     * @param value rest of packet string
     */
    public void writeStringEOF(final String value) {
        byteBuf.writeBytes(value.getBytes(charset));
    }

    /**
     * Skip reserved from byte buffers.
     *
     * @param length length of reserved
     */
    public void skipReserved(final int length) {
        byteBuf.skipBytes(length);
    }

    /**
     * Write null for reserved to byte buffers.
     *
     * @param length length of reserved
     */
    public void writeReserved(final int length) {
        byteBuf.writeZero(length);
    }

    @Override
    public void close() throws Exception {
        byteBuf.release();
    }
}
