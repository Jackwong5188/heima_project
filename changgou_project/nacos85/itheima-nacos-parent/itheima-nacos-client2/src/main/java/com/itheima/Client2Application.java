package com.itheima;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Client2Application {
    public static void main(String[] args) {
        SpringApplication.run(Client2Application.class,args);
    }

    @RestController
    @RequestMapping("/config")
    @RefreshScope  //启用刷新配置
    public class ConfigController {

        @Value("${useLocalCache:false}") //useLocalCache 默认值： false
        private boolean useLocalCache;

        @RequestMapping("/get")
        public boolean get() {
            return useLocalCache;
        }
    }

}
