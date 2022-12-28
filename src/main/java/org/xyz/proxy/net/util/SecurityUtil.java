package org.xyz.proxy.net.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加解密 校验密码相关
 */
public class SecurityUtil {

    // mysql_native_password
    // SHA1( password ) XOR SHA1( "20-bytes random data from server" <concat> SHA1( SHA1( password ) ) )
    public static byte[] scramble411(byte[] pass, byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        // SHA1(src) => digest_stage1
        byte[] pass1 = md.digest(pass);
        md.reset();
        // SHA1(digest_stage1) => digest_stage2
        byte[] pass2 = md.digest(pass1);
        md.reset();
        // SHA1(m_rnd, digest_stage2) => scramble_stage1
        md.update(seed);
        byte[] pass3 = md.digest(pass2);
        // XOR(digest_stage1, scramble_stage1) => out_scramble
        for (int i = 0; i < pass3.length; i++) {
            pass3[i] = (byte) (pass3[i] ^ pass1[i]);
        }
        return pass3;
    }

    // clientPass是客户端传来的加密字符串
    // password是服务端存储的sha1(sha1(明文密码))
    // 返回sha1(sha1(明文密码))
    public static byte[] decodeClientPassword(byte[] clientPass, byte[] seed, byte[] password) throws NoSuchAlgorithmException {
        // clientPass = SHA1( 明文密码 ) XOR SHA1( seed + SHA1( SHA1( 明文密码 ) ) )
        // password = SHA1( SHA1( 明文密码 ) )
        // 将客户端传来的clientPass，经过 (clientPass XOR SHA1(seed + password)) = SHA1( 明文密码 )
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        // SHA1(seed + password) -> pass1 -> SHA1( seed + SHA1( SHA1( 明文密码 ) ) )
        md.update(seed);
        byte[] pass1 = md.digest(password);
        // clientPass XOR pass1 -> pass2 = SHA1( 明文密码 )
        byte[] pass2 = new byte[clientPass.length];
        for (int i = 0; i < clientPass.length; i++) {
            pass2[i] = (byte) (clientPass[i] ^ pass1[i]);
        }

        // SHA1( pass2 ) -> pass4 = SHA1( SHA1( 明文密码 ) )
        md.reset();
        byte[] pass4 = md.digest(pass2);
        return pass4;
    }

    // mysql_native_password 在 mysql.user 表中 authentication_string 字段存储的是两次哈希 SHA1(SHA1(password)) 计算的值，以十六进制byte[]形式存储
    public static byte[] scrambleSha1Sha1(byte[] pass) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        // SHA1(src) => digest_stage1
        byte[] pass1 = md.digest(pass);
        md.reset();
        // SHA1(digest_stage1) => digest_stage2
        byte[] pass2 = md.digest(pass1);
        return pass2;
    }

    public static String scramble323(String pass, String seed) {
        if ((pass == null) || (pass.length() == 0)) {
            return pass;
        }
        byte b;
        double d;
        long[] pw = hash(seed);
        long[] msg = hash(pass);
        long max = 0x3fffffffL;
        long seed1 = (pw[0] ^ msg[0]) % max;
        long seed2 = (pw[1] ^ msg[1]) % max;
        char[] chars = new char[seed.length()];
        for (int i = 0; i < seed.length(); i++) {
            seed1 = ((seed1 * 3) + seed2) % max;
            seed2 = (seed1 + seed2 + 33) % max;
            d = (double) seed1 / (double) max;
            b = (byte) Math.floor((d * 31) + 64);
            chars[i] = (char) b;
        }
        seed1 = ((seed1 * 3) + seed2) % max;
        seed2 = (seed1 + seed2 + 33) % max;
        d = (double) seed1 / (double) max;
        b = (byte) Math.floor(d * 31);
        for (int i = 0; i < seed.length(); i++) {
            chars[i] ^= (char) b;
        }
        return new String(chars);
    }

    private static long[] hash(String src) {
        long nr = 1345345333L;
        long add = 7;
        long nr2 = 0x12345671L;
        long tmp;
        for (int i = 0; i < src.length(); ++i) {
            switch (src.charAt(i)) {
                case ' ':
                case '\t':
                    continue;
                default:
                    tmp = (0xff & src.charAt(i));
                    nr ^= ((((nr & 63) + add) * tmp) + (nr << 8));
                    nr2 += ((nr2 << 8) ^ nr);
                    add += tmp;
            }
        }
        long[] result = new long[2];
        result[0] = nr & 0x7fffffffL;
        result[1] = nr2 & 0x7fffffffL;
        return result;
    }

    // TODO: caching_sha2_password
    // XOR(SHA256(password), SHA256(SHA256(SHA256(password)), Nonce))
    // Nonce - 20 byte long random data
    public static byte[] scrambleSha2(byte[] pass, byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // SHA2(src) => digest_stage1
        byte[] pass1 = md.digest(pass);
        md.reset();
        // SHA2(digest_stage1) => digest_stage2
        byte[] pass2 = md.digest(pass1);
        md.reset();
        // SHA2(digest_stage2, m_rnd) => scramble_stage1
        md.update(pass2);
        byte[] pass3 = md.digest(seed);
        // XOR(digest_stage1, scramble_stage1) => out_scramble
        for (int i = 0; i < pass3.length; i++) {
            pass3[i] = (byte) (pass3[i] ^ pass1[i]);
        }
        return pass3;
    }
    public static void main(String[] args) throws NoSuchAlgorithmException {
        byte[] seed = new byte[] {(byte) 0x59, (byte) 0x4a, (byte) 0x45, (byte) 0x37, (byte) 0x32, (byte) 0x69, (byte) 0x43, (byte) 0x38, (byte) 0x42, (byte) 0x47, (byte) 0x73, (byte) 0x73, (byte) 0x54, (byte) 0x6e, (byte) 0x77, (byte) 0x39, (byte) 0x54, (byte) 0x72, (byte) 0x4d, (byte) 0x77};
        byte[] clientPass = SecurityUtil.scramble411("test".getBytes(), seed);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("clientPass: ");
        for(byte i : clientPass) {
            sb1.append(Integer.toHexString(i & 0xFF)).append(", ");
        }
        sb1.append('\n');
        System.out.println(sb1);

        StringBuilder sb = new StringBuilder();

        byte[] password = SecurityUtil.scrambleSha1Sha1("test".getBytes());
        sb.append("server password: ");
        for(byte i : password) {
            sb.append(Integer.toHexString(i & 0xFF)).append(", ");
        }
        sb.append('\n');

        System.out.println(sb);

        StringBuilder sb2 = new StringBuilder();
        sb2.append("client password: ");

        byte[] g = decodeClientPassword(clientPass, seed, password);

        for(byte i : g) {
            sb2.append(Integer.toHexString(i & 0xFF)).append(", ");
        }
        sb2.append('\n');
        System.out.println(sb2);

    }
}
