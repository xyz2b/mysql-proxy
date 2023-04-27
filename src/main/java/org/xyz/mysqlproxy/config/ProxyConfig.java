package org.xyz.mysqlproxy.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ProxyConfig {
    @Value("${proxy.ip}")
    private String ip;

    @Value("${proxy.port}")
    private int port;

    @Value("${proxy.default-auth-plugin}")
    private String defaultAuthPlugin;
}
