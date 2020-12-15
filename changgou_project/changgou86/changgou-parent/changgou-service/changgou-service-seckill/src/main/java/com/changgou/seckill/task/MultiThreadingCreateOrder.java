package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.IdWorker;
import entity.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    /***
     * 多线程下单操作
     */
    @Async  //异步
    public void createOrder(){
        //从Redis队列中获取seckillStatus
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();

        try {
            //时间区间
            //String time = "2020112614";
            String time = seckillStatus.getTime();
            //用户登录名
            //String username="zhangsan";
            String username = seckillStatus.getUsername();
            //用户抢购商品
            //Long id = 1131814839107325952L;
            Long id = seckillStatus.getGoodsId();

            //获取商品数据
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + time).get(id);

            //如果没有库存，则直接抛出异常
            /*if(seckillGoods==null || seckillGoods.getStockCount()<=0){
                throw new RuntimeException("已售罄!");
            }*/
            //如果有库存，则创建秒杀商品订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());   //主键id
            seckillOrder.setSeckillId(id);   //秒杀商品ID
            seckillOrder.setMoney(seckillGoods.getCostPrice());  //支付金额
            seckillOrder.setUserId(username);  //用户
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0"); //状态，0未支付，1已支付

            //将秒杀订单存入到Redis中
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).put(username,seckillOrder);

            //库存减少
            /*seckillGoods.setStockCount(seckillGoods.getStockCount()-1);*/

            //判断当前商品是否还有库存
            if(seckillGoods.getStockCount()<=0){
                //将商品数据同步到MySQL中
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //没有库存,则清空Redis缓存中该商品
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).delete(id);
            }else {
                //有库存，则将商品数据重置到Reids中
                /*redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).put(id,seckillGoods);*/
            }

            //抢单成功，更新抢单状态,排队: 1 ->等待支付: 2
            seckillStatus.setStatus(2);
            seckillStatus.setOrderId(seckillOrder.getId());  //订单号
            seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney()));  //应付金额
            //更新下单状态, 并存入到Redis中
            //redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).put(username,seckillStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
