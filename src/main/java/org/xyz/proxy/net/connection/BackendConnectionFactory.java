package org.xyz.proxy.net.connection;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class BackendConnectionFactory {
    public BackendConnectionFactory() {

    }

    /**
     * MySql ThreadId Generator
     */
    private static final AtomicInteger ACCEPT_SEQ = new AtomicInteger(0);

    public BackendConnection getConnection() {
        BackendConnection connection = new BackendConnection();
        connection.setId(ACCEPT_SEQ.getAndIncrement());
        log.info("connection Id="+connection.getId());
        connection.setCharset("utf8_bin");
        return connection;
    }
}
