package com.tensquare.notice.netty.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import com.tensquare.notice.config.ApplicationContextProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    public static ConcurrentHashMap<String,Channel> map = new ConcurrentHashMap<String,Channel>();

    private static SimpleMessageListenerContainer simpleMessageListenerContainer = ApplicationContextProvider.getApplicationContext().getBean("sysNoticeContainer", SimpleMessageListenerContainer.class);

    RabbitAdmin rabbitAdmin = ApplicationContextProvider.getApplicationContext().getBean("rabbitAdmin", RabbitAdmin.class);

    //channelHandlerContext:信道处理上下文对象
    //textWebSocketFrame: 数据对象【约定用户第一次连接传递数据{"userId":"1"}】
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //1.获得用户传输的数据
        String data = textWebSocketFrame.text();
        //2.解析数据获得userId
        String userId = (String) JSONObject.parseObject(data).get("userId");
        //3.判断是否是第一次连接
        Channel channel = map.get(userId);
        //4.如果是, 建立连接, 把连接存到Map
        if(channel == null){
            channel = channelHandlerContext.channel();
            map.put(userId,channel);
        }
        //5.获得当前用户队列的消息(数量)
        String queueName = "article_subscribe_" + userId;//队列名字
        Properties properties = rabbitAdmin.getQueueProperties(queueName);//队列的配置对象

        int noticeCount = 0;
        if(properties != null){
            noticeCount = (int) properties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
        }
        //6.把数据发送给用户
        HashMap countMap = new HashMap();
        countMap.put("sysNoticeCount", noticeCount);
        Result result = new Result(true, StatusCode.OK, "查询成功", countMap);
        TextWebSocketFrame msg = new TextWebSocketFrame(JSONObject.toJSONString(result));
        channel.writeAndFlush(msg);
        //7.消费消息
        if (noticeCount > 0) {
            rabbitAdmin.purgeQueue(queueName,true);
        }
        //8.设置队列的监听
        simpleMessageListenerContainer.addQueueNames(queueName);
    }
}
