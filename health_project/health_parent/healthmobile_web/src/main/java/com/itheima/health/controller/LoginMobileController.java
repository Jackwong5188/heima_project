package com.itheima.health.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.constant.RedisMessageConstant;
import com.itheima.health.entity.Result;
import com.itheima.health.pojo.Member;
import com.itheima.health.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * 用户登录
 */
@RestController
@RequestMapping("/login")
public class LoginMobileController {
    @Reference
    MemberService memberService;

    // Redis本机（单机）
    //@Autowired
    //private JedisPool jedisPool;

    // Redis集群
    @Autowired
    RedisTemplate redisTemplate;

    //使用手机号和验证码登录
    @RequestMapping("/check")
    public Result check(HttpServletResponse response, @RequestBody Map map){
        //获取用户输入的手机号
        String telephone = (String) map.get("telephone");
        //获取用户输入的验证码
        String validateCode = (String) map.get("validateCode");
        //从Redis中获取缓存的验证码，key为手机号+RedisConstant.SENDTYPE_ORDER
        //String codeInRedis = jedisPool.getResource().get(telephone + RedisMessageConstant.SENDTYPE_LOGIN);
        String codeInRedis = (String) redisTemplate.opsForValue().get(telephone + RedisMessageConstant.SENDTYPE_LOGIN);
        //校验手机验证码
        if(codeInRedis == null || !codeInRedis.equals(validateCode)){ // Redis中获取缓存的验证码 与 用户输入的验证码 不相等
            return new Result(false, MessageConstant.VALIDATECODE_ERROR);
        }else {
            //验证码输入正确
            //2：判断当前用户是否为会员
            Member member = memberService.findMemberByTelephone(telephone);   //根据手机号查找会员
            if(member == null){
                //当前用户不是会员，自动完成注册
                member = new Member();
                member.setPhoneNumber(telephone);   //用户手机号
                member.setRegTime(new Date());   //注册日期
                //新增会员
                memberService.add(member);
            }
            //3：:登录成功
            //写入Cookie，跟踪用户，用于分布式系统单点登录
            Cookie cookie = new Cookie("login_member_telephone", telephone);
            cookie.setPath("/");  //路径
            cookie.setMaxAge(60*60*24*30); //有效期30天（单位秒）
            response.addCookie(cookie);
            return new Result(true,MessageConstant.LOGIN_SUCCESS);
        }
    }
}
