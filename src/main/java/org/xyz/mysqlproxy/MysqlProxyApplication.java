package org.xyz.mysqlproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.xyz.mysqlproxy.net.server.ProxyServer;

@Configuration
//自动加载配置信息
//使包路径下带有@Value的注解自动注入
//使包路径下带有@Autowired的类可以自动注入
@SpringBootApplication
public class MysqlProxyApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MysqlProxyApplication.class, args);

        /**
         * 启动服务
         */
        ProxyServer nettyServer = context.getBean(ProxyServer.class);
        try {
            nettyServer.run();
        } catch (Exception e) {
            context.close();
            System.exit(-1);
        }
        context.close();
        System.exit(0);
    }

}
