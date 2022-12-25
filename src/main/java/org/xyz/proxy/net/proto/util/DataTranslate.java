package org.xyz.proxy.net.proto.util;

// JVM 默认大端字节序，即高位存储在低地址，低位存储在高地址
public class DataTranslate {
    /**
     * 将u2字节数组(大端字节序)转成UnsignedShort数值(short正好占两个字节)
     * 如 300(十进制) 0x012C(十六进制) 分两个字节大端字节序存储，就是bytes[0] = 0x01，bytes[1] = 0x2C
     * 解析时就需要把这两个字节组合成 0x012C，只需要将bytes[0]左移8位，然后和bytes[1]进行或操作即可
     * @param bytes 2个字节大小的字节数组
     * @return 转换后的UnsignedShort数值
     * */
    public static int byteToUnsignedShort(byte[] bytes) { return byteToUnsignedShort(bytes, false); }
    /**
     * @param littleEndian 是否是小端字节序
     * */
    public static int byteToUnsignedShort(byte[] bytes, boolean littleEndian) {
        int high;
        int low;
        if (littleEndian) {
            high = bytes[1];
            // 低字节
            low = bytes[0];
        } else {
            // 高字节
            high = bytes[0];
            // 低字节
            low = bytes[1];
        }

        return ((high & 0xFF) << 8) | (low & 0xFF);
    }

    /**
     * 将UnsignedShort数值(short正好占两个字节)转成u2字节数组(大端字节序)
     * 如 300(十进制) 0x012C(十六进制) 分两个字节大端字节序存储，就是bytes[0] = 0x01，bytes[1] = 0x2C
     * 只需要将UnsignedShort数值分别与0xFF00、0x00FF做与操作得到对应位置上的字节，然后再分别右移8位、0位
     * @param value UnsignedShort数值(
     * @return 转换后的UnsignedShort数值
     * */
    public static byte[] unsignedShortToByte(int value) { return unsignedShortToByte(value, false); }
    public static byte[] unsignedShortToByte(int value, boolean littleEndian) {
        byte[] bytes = new byte[2];
        if (littleEndian) {
            // 高字节
            bytes[1] = (byte) ((value & 0xFF00) >>> 8);
            // 低字节
            bytes[0] = (byte) (value & 0x00FF);
        } else {
            // 高字节
            bytes[0] = (byte) ((value & 0xFF00) >>> 8);
            // 低字节
            bytes[1] = (byte) (value & 0x00FF);
        }
        return bytes;
    }

    /**
     * 将u4字节数组(大端字节序)转成int数值(int正好占四个字节)
     * 如 3000000(十进制) 0x002D_C6C0(十六进制) 分四个字节大端字节序存储，就是bytes[0] = 0x00，bytes[1] = 0x2D，bytes[2] = 0xC6，bytes[3] = 0xC0
     * 解析时就需要把这四个字节组合成 0x002D_C6C0，只需要将bytes[0]左移24位，bytes[1]左移16位，bytes[2]左移8位，然后和bytes[3]进行与操作
     * @param bytes 4个字节大小的字节数组
     * @return 转换后的int数值
     * */
    public static int byteToInt(byte[] bytes) { return byteToInt(bytes, false); }
    public static int byteToInt(byte[] bytes, boolean littleEndian) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            // << 3 相当于 乘以 8
            int shift = littleEndian ? i << 3 : (3 - i) << 3;
            value |= (bytes[i] & (int) 0xFF) << shift;
        }
        return value;
    }

    /**
     * 将int数值(int正好占四个字节)转成u4字节数组(大端字节序)
     * 如 3000000(十进制) 0x002D_C6C0(十六进制) 分四个字节大端字节序存储，就是bytes[0] = 0x00，bytes[1] = 0x2D，bytes[2] = 0xC6，bytes[3] = 0xC0
     * 只需要将int数值分别与0xFF00_0000、0x00FF_000、0x0000_FF00、0x0000_00FF做与操作得到对应位置上的字节，然后再分别右移24位、16位、8位、0位
     * @param value int数值
     * @return 转换后的byte数组
     * */
    public static byte[] intToByte(int value) { return intToByte(value, false); }
    public static byte[] intToByte(int value, boolean littleEndian) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            // << 3 相当于 乘以 8
            int shift = littleEndian ? i << 3 : (3 - i) << 3;
            bytes[i] = (byte) ((value & ((int) 0xFF << shift)) >>> shift);
        }
        return bytes;
    }

    /**
     * 将float数值(4个字节)转成u4字节数组(大端字节序)
     * 先将float值(4字节)转成对应bit都相等的int值(4字节)(Float.floatToIntBits所做的事情)，然后再将int转为byte数组
     * 如 10.11(十进制float) --> 0x4121_C28F(十六进制) --> 1092731535(十进制int) --> byte[]
     * @param value float数值
     * @return 转换后的byte数组
     * */
    public static byte[] floatToByte(float value) { return floatToByte(value, false); }
    public static byte[] floatToByte(float value, boolean littleEndian) {
        // Float.floatToIntBits 默认返回的是大端字节序
        int intBits = Float.floatToRawIntBits(value);
//        outBytesByHex(intToByte(intBits, littleEndian));
        return intToByte(intBits, littleEndian);
    }

    /**
     * 将u4字节数组(大端字节序)转成float数值(4个字节)
     * 先将bytes数组转成int，然后将int值(4字节)转成对应bit都相等的float值(4字节)(Float.intBitsToFloat所做的事情)
     * 如 byte[] --> 1092731535(十进制int) --> 0x4121_C28F(十六进制) --> 10.11(十进制float)
     * @param bytes 4个字节大小的字节数组
     * @return 转换后的float数值
     * */
    public static float byteToFloat(byte[] bytes) { return byteToFloat(bytes, false); }
    public static float byteToFloat(byte[] bytes, boolean littleEndian) {
        if (littleEndian) {
            reverse(bytes);
        }
        int intBits = byteToInt(bytes);
        // Float.intBitsToFloat 默认是按照大端字节序解析
        return Float.intBitsToFloat(intBits);
    }

    /**
     * 将u8字节数组(大端字节序)转成long数值(long正好占八个字节)
     * @param bytes 8个字节大小的字节数组
     * @return 转换后的long数值
     * */
    public static long byteToLong(byte[] bytes) { return byteToLong(bytes, false); }
    public static long byteToLong(byte[] bytes, boolean littleEndian) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            // << 3 相当于 乘以 8
            int shift = littleEndian ? i << 3 : (7 - i) << 3;
            value |= (bytes[i] & (long) 0xFF) << shift;
        }
        return value;
    }

    /**
     * 将long数值(long正好占八个字节)转成u8字节数组(大端字节序)
     * @param value long数值
     * @return 转换后的byte数组
     * */
    public static byte[] longToByte(long value) { return longToByte(value, false); }
    public static byte[] longToByte(long value, boolean littleEndian) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            // << 3 相当于 乘以 8
            int shift = littleEndian ? i << 3 : (7 - i) << 3;
            // 如果这里的0xFF不强制转成long类型，会默认被当成int类型对待
            // Java 中的 << 是循环左移
            // 将int类型的0xFF，左移7个字节(循环一次)，结果是0xFF00_0000
            // 而long类型的0xFF，左移7个字节，结果是0xFF00_0000_0000_0000
            bytes[i] = (byte) ((value & ((long) 0xFF << shift)) >>> shift);
        }
        return bytes;
    }

    /**
     * 将u8字节数组(大端字节序)转成double数值(double正好占八个字节)
     * @param bytes 字节数组
     * @return 转换后的double值
     * */
    public static double byteToDouble(byte[] bytes) { return byteToDouble(bytes, false); }
    public static double byteToDouble(byte[] bytes, boolean littleEndian) {
        if (littleEndian) {
            reverse(bytes);
        }
        long longBits = byteToLong(bytes);
        // Double.longBitsToDouble 默认是按照大端字节序解析
        return Double.longBitsToDouble(longBits);
    }

    /**
     * 将double数值(double正好占八个字节)转成u8字节数组(大端字节序)
     * @param value double值
     * @return 转换后的字节数组
     * */
    public static byte[] doubleToByte(double value) { return doubleToByte(value, false); }
    public static byte[] doubleToByte(double value, boolean littleEndian) {
        // Double.doubleToRawLongBits 默认返回的是大端字节序
        long longBits = Double.doubleToRawLongBits(value);
        return longToByte(longBits, littleEndian);
    }

    /**
     * 反转字节数组
     * @param arr 待反转的字节数组
     * @return 反转后的字节数组
     * */
    public static byte[] reverse(byte[] arr) {
        int length = arr.length;
        byte temp;
        for (int i = 0; i < length / 2; i++) {
            temp = arr[i];
            arr[i] = arr[length - i - 1];
            arr[length - i - 1] = temp;
        }
        return arr;
    }

    /**
     * 以16进制形式打印byte数组中的每个byte
     * @param bytes 需要打印输出的字节数组
     * */
    public static String outBytesByHex(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        out.append("[");
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                if (i == bytes.length - 1) {
                    out.append(String.format("0x%X", bytes[i]));
                } else {
                    out.append(String.format("0x%X, ", bytes[i]));
                }
            }
        }
        out.append("]");
        return out.toString();
    }

    /**
     * 将byte转成String
     * @param b byte类型的值
     * @return 转换后的String
     * */
    public static String byteToString(byte b) {
        return new String(new byte[] {b});
    }
}
