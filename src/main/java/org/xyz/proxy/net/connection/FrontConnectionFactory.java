package org.xyz.proxy.net.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class FrontConnectionFactory {
    public FrontConnectionFactory() {

    }

    /**
     * MySql ThreadId Generator
     */
    private static final AtomicInteger ACCEPT_SEQ = new AtomicInteger(0);

    public FrontendConnection getConnection() {
        FrontendConnection connection = new FrontendConnection();
        connection.setId(ACCEPT_SEQ.getAndIncrement());
        log.info("connection Id="+connection.getId());
        connection.setCharset("utf8_bin");
        return connection;
    }
}
