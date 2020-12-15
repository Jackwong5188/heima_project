package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.SystemConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/***
 * 监听秒杀的队列
 * @author ljh
 * @packagename com.changgou.seckill.consumer
 * @version 1.0
 * @date 2020/4/5
 */
@Component
@RabbitListener(queues = "queue.seckillorder")
public class SeckillOrderPayMessageListener {
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 监听消费消息
     * @param msg
     */
    @RabbitHandler
    public void handler(String msg){
        System.out.println("=====SeckillOrderPayMessageListener类handler()方法输出语句，接受RabbitMQ中的消息=====");
        System.out.println(msg);//接受RabbitMQ中的消息，msg就是WeixinPayController类(支付回调)notifyUrl方法中 convertAndSend()方法中的参数：JSON.toJSONString(map)
        //将消息转换成Map对象
        Map<String,String> map = JSON.parseObject(msg, Map.class);
        String out_trade_no = map.get("out_trade_no");  //订单号
        String attach = map.get("attach");  //附加数据, {from:2,usrename:}
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);

        if (map != null && map.get("return_code").equals("SUCCESS") && map.get("result_code").equals("SUCCESS")) {
                //1.支付成功
                //1.2 (根据用户名)获取秒杀订单   "SeckillOrder"
                SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).get(attachMap.get("username"));
                //1.3 更新订单
                seckillOrder.setStatus("1");  //状态，0未支付，1已支付
                seckillOrder.setTransactionId(map.get("transaction_id"));  //交易流水

                String time_end = map.get("time_end"); //支付时间
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                try {
                    Date parse = simpleDateFormat.parse(time_end);
                    seckillOrder.setPayTime(parse);//设置支付时间
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //1.4 订单入库
                seckillOrderMapper.insertSelective(seckillOrder);
                //1.5 清空redis中的订单   "SeckillOrder"
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(attachMap.get("usrename"));
                //1.6 删除排队信息    "UserQueueCount"
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_QUEUE_REPEAT_KEY).delete(attachMap.get("username"));
                //1.7 删除抢单信息    "UserQueueStatus"
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).delete(attachMap.get("username"));
        }else {
            // 支付失败
            //2.1 (根据用户名)获取秒杀状态    "UserQueueStatus"
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).get(attachMap.get("usrename"));
            //2.2 从Redis中获取秒杀商品   "SeckillGoods_"
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
            if(seckillGoods==null){  //没有，还要从数据库查询
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            }

            RLock mylock = redissonClient.getLock("Mylock");
            try {
                mylock.lock(100, TimeUnit.SECONDS);  //上锁
                //2.3 库存数量+1
                seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
                //2.4 将秒杀商品存入Redis中    "SeckillGoods_"
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+seckillStatus.getTime()).put(seckillStatus.getGoodsId(),seckillGoods);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                mylock.unlock();  //解锁
            }

            //2.5 清空redis中的订单   "SeckillOrder"
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(attachMap.get("usrename"));
            //2.6 删除排队信息    "UserQueueCount"
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_QUEUE_REPEAT_KEY).delete(attachMap.get("username"));
            //2.7 删除抢单信息    "UserQueueStatus"
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).delete(attachMap.get("username"));
        }
    }
}
