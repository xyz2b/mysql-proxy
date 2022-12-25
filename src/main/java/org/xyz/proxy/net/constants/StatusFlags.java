package org.xyz.proxy.net.constants;

public class StatusFlags {
    public static final short CLIENT_LONG_PASSWORD = 1;
    public static final short SERVER_STATUS_AUTOCOMMIT = 1 << 1;


    public static void main(String[] args) {
        System.out.println(1 << 1);
    }
}
