package com.tensquare.article.conf;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Rabbitmq配置
 */
@Configuration
public class RabbitmqConfig {

    //交换机的名称
    public static final String EX_ARTICLE = "tensquare_subscribe";

    /**
     * 创建Rabbit管理器
     * @param connectionFactory
     * @return
     */
    @Bean("rabbitAdmin")
    public RabbitAdmin createRabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        return rabbitAdmin;
    }

    /**
     * 创建DirectExchange类型交换机
     * @return the exchange
     */
    @Bean("directExchange")
    public DirectExchange createDirectExchange(RabbitAdmin rabbitAdmin) {
        DirectExchange exchange = new DirectExchange(EX_ARTICLE);
        rabbitAdmin.declareExchange(exchange);
        return exchange;
    }

    /**
     * 创建消息监听器容器
     * @param connectionFactory
     * @return
     */
    @Bean("sysNoticeContainer")
    public SimpleMessageListenerContainer createMessageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container =
                new SimpleMessageListenerContainer(connectionFactory);
        //使用Channel
        container.setExposeListenerChannel(true);
        //设置自己编写的监听器 TODO
        return container;
    }
}
