package com.itheima.health.controller;

import com.aliyuncs.exceptions.ClientException;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.constant.RedisMessageConstant;
import com.itheima.health.entity.Result;
import com.itheima.health.utils.SMSUtils;
import com.itheima.health.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码
 */
@RestController
@RequestMapping("/validateCode")
public class ValidateCodeController {
    // Redis本机（单机）
    //@Autowired
    //JedisPool jedisPool;

    // Redis集群
    @Autowired
    RedisTemplate redisTemplate;

    //体检预约时发送手机验证码
    @RequestMapping("/send4Order")
    public Result send4Order(String telephone){
        Integer code = ValidateCodeUtils.generateValidateCode(4); //生成4位数字验证码
        try {
            //发送短信
            SMSUtils.sendShortMessage(telephone,code.toString());
        } catch (ClientException e) {
            e.printStackTrace();
            //验证码发送失败
            return new Result(false, MessageConstant.SEND_VALIDATECODE_FAIL);
        }
        System.out.println("发送的手机验证码为：" + code);
        /**
         * 2：将手机号和验证码存放到redis中
         key                              value                     失效时间（秒）
         13212341234001                  1122                          5*60
         */
        //jedisPool.getResource().setex(telephone+RedisMessageConstant.SENDTYPE_ORDER,5 * 60,code.toString());
        redisTemplate.opsForValue().set(telephone+RedisMessageConstant.SENDTYPE_ORDER,code.toString(),5, TimeUnit.MINUTES);
        //验证码发送成功
        return new Result(true, MessageConstant.SEND_VALIDATECODE_SUCCESS);
    }

    //手机快速登录时发送手机验证码
    @RequestMapping("/send4Login")
    public Result send4Login(String telephone){
        Integer code = ValidateCodeUtils.generateValidateCode(4); //生成4位数字验证码
        try {
            //发送短信
            SMSUtils.sendShortMessage(telephone,code.toString());
        } catch (ClientException e) {
            e.printStackTrace();
            //验证码发送失败
            return new Result(false, MessageConstant.SEND_VALIDATECODE_FAIL);
        }
        System.out.println("发送的手机验证码为：" + code);
        /**
         * 2：将手机号和验证码存放到redis中
         key                              value                     失效时间（秒）
         13212341234001                  1122                          5*60
         */
        //jedisPool.getResource().setex(telephone+RedisMessageConstant.SENDTYPE_LOGIN,5 * 60,code.toString());
        redisTemplate.opsForValue().set(telephone+RedisMessageConstant.SENDTYPE_LOGIN,code.toString(),5, TimeUnit.MINUTES);
        //验证码发送成功
        return new Result(true, MessageConstant.SEND_VALIDATECODE_SUCCESS);
    }
}
