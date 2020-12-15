package com.changgou;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableEurekaClient
public class WeixinPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeixinPayApplication.class,args);
    }

    @Autowired
    private Environment environment;

    //创建队列
    @Bean
    public Queue createQueue(){
        // queue.order
        return new Queue(environment.getProperty("mq.pay.queue.order"));
    }

    //创建交互机 路由模式的交换机
    @Bean
    public DirectExchange createExchange(){
        // exchange.order
        return new DirectExchange(environment.getProperty("mq.pay.exchange.order"));
    }

    //创建绑定, 绑定队列到交换机
    @Bean
    public Binding createBinding(){
        // routing key : queue.order
        String property = environment.getProperty("mq.pay.routing.key");
        return BindingBuilder.bind(createQueue()).to(createExchange()).with(property);
    }

    //创建秒杀队列
    @Bean
    public Queue createSeckillQueue(){
        // queue.seckillorder
        return new Queue(environment.getProperty("mq.pay.queue.seckillorder"));
    }

    //创建秒杀交互机 路由模式的交换机
    @Bean
    public DirectExchange createSeckillExchange(){
        // exchange.seckillorder
        return new DirectExchange(environment.getProperty("mq.pay.exchange.seckillorder"));
    }

    //创建秒杀绑定, 绑定队列到交换机
    @Bean
    public Binding createSeckillBinding(){
        // routing seckillkey : queue.seckillorder
        String property = environment.getProperty("mq.pay.routing.seckillkey");
        return BindingBuilder.bind(createSeckillQueue()).to(createSeckillExchange()).with(property);
    }
}