package com.changgou.seckill.service.impl;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.task.MultiThreadingCreateOrder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import entity.SystemConstants;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/****
 * @Author:admin
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    private RedissonClient redissonClient;

    /***
     * 添加秒杀订单
     * @param id:商品ID
     * @param time:参与秒杀商品的时间段
     * @param username:用户登录名
     * @return
     */
    @Override
    public Boolean add(Long id, String time, String username) {
        //根据username设置自增值userQueueCount，初始值为1，每次进入抢单的时候，对它进行递增。
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        //如果值>1，则表明已经排队, 则对外抛出异常。
        if(userQueueCount>1){
            throw new RuntimeException("重复排队了");
        }

        //避免超卖--> 使用分布式锁
        RLock mylock = redissonClient.getLock("Mylock");
        try {
            //上锁
            mylock.lock(100, TimeUnit.SECONDS);
            //减库存
            deccount(id, time);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            //解锁
            mylock.unlock();
        }

        //排队信息封装到seckillStatus
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);

        //将seckillStatus存入到Redis中,这里采用List方式存储,List本身是一个队列
        //redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);
        redisTemplate.boundListOps(SystemConstants.SEC_KILL_USER_QUEUE_KEY).leftPush(seckillStatus);

        //更新下单状态, 并存入到Redis中
        //redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);
        redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).put(username,seckillStatus);

        //多线程操作
        multiThreadingCreateOrder.createOrder();

        /*//获取商品数据
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + time).get(id);

        //如果没有库存，则直接抛出异常
        if(seckillGoods==null || seckillGoods.getStockCount()<=0){
            throw new RuntimeException("已售罄!");
        }
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
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

        //判断当前商品是否还有库存
        if(seckillGoods.getStockCount()<=0){
            //将商品数据同步到MySQL中
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
            //没有库存,则清空Redis缓存中该商品
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).delete(id);
        }else {
            //有库存，则将商品数据重置到Reids中
            redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).put(id,seckillGoods);
        }*/
        return true;
    }

    /*
     * 减库存
     * @param id:商品ID
     * @param time:参与秒杀商品的时间段
     */
    private void deccount(Long id, String time) {
        // namespace = SeckillGoods_2019010112
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + time).get(id);
        //如果没有库存，则直接抛出异常
        if (seckillGoods == null || seckillGoods.getStockCount() <= 0) {
            throw new RuntimeException("已售罄!");
        }
        //库存减少
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        //有库存，则将商品数据重置到Reids中
        redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX+time).put(id,seckillGoods);

        System.out.println("库存为：" + seckillGoods.getStockCount());
    }

    /****
     * 查询抢单状态
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        return seckillStatus;
    }
}
