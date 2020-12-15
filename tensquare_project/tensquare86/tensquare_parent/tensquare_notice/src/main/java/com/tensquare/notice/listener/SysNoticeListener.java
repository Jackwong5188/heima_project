package com.tensquare.notice.listener;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import com.tensquare.notice.netty.handler.MyWebSocketHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

import java.util.HashMap;

public class SysNoticeListener implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //1.获得队列名称
        String queueName = message.getMessageProperties().getConsumerQueue();
        //2.获得userId
        String userId = queueName.substring(queueName.lastIndexOf("_") + 1);
        //3.获得信道
        io.netty.channel.Channel channel1 = MyWebSocketHandler.map.get(userId);
        //4.判断用户是否在线
        if(channel1 != null){
            //5.发送消息
            HashMap countMap = new HashMap();
            countMap.put("sysNoticeCount", 1);
            Result result = new Result(true, StatusCode.OK, "查询成功", countMap);
            // 把数据通过WebSocket连接主动推送用户
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSONObject.toJSONString(result));
            channel1.writeAndFlush(textWebSocketFrame);
        }
    }
}
