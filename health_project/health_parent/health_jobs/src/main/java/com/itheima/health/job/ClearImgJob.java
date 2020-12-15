package com.itheima.health.job;

import com.itheima.health.constant.RedisConstant;
import com.itheima.health.utils.QiniuUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisPool;

import java.util.Iterator;
import java.util.Set;

/**
 * 定时任务：清理垃圾图片
 */
public class ClearImgJob {
    @Autowired
    JedisPool jedisPool;

    //清理图片
    public void clearImg() {
        //计算redis中两个集合的差值，获取垃圾图片名称
        Set<String> set = jedisPool.getResource().sdiff(RedisConstant.SETMEAL_PIC_RESOURCE, RedisConstant.SETMEAL_PIC_DB_RESOURCE);
        // 遍历
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()){
            String img = iterator.next();
            System.out.println("需要删除的图片名称：" + img);
            // 删除七牛云上的图片
            QiniuUtils.deleteFileFromQiniu(img);
            // 删除(Redis中)key值是setmeal_pic_resource的值
            jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_RESOURCE,img);
        }
    }
}
