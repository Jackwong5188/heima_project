package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/***
 * 描述
 * @author ljh
 * @packagename com.changgou
 * @version 1.0
 * @date 2020/3/23
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//不需要自动配置数据库
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.changgou.goods.feign")//启用feignclient,指定要扫描的feign所在的包名即可
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class,args);
    }
}