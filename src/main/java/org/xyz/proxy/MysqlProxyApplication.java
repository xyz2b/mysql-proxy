package org.xyz.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.xyz.proxy.net.server.MysqlServer;

@Configuration
//自动加载配置信息
//使包路径下带有@Value的注解自动注入
//使包路径下带有@Autowired的类可以自动注入
@ComponentScan("org.xyz.proxy")
@SpringBootApplication
public class MysqlProxyApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(MysqlProxyApplication.class, args);

        /**
         * 启动服务
         */
        MysqlServer nettyServer = context.getBean(MysqlServer.class);
        nettyServer.run();
    }

}
