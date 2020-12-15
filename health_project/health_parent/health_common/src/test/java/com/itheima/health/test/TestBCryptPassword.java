package com.itheima.health.test;

import com.itheima.health.utils.MD5Utils;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCryptPassword {
    @Test
    public void bCryptPassword(){
        // 新增注册用户
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String s1 = bCryptPasswordEncoder.encode("123");
        System.out.println(s1);

        String s2 = bCryptPasswordEncoder.encode("123");
        System.out.println(s2);

        // 登录页面匹配，第一个参数，页面输入的密码；第二个参数，从数据库查询的密码
        boolean matches = bCryptPasswordEncoder.matches("123", "$2a$10$gLpQUw7EZ3nuC.kpaVRsmOkjivVeTOfoDMGc5LGNJtVaHn/nGkQJq");
        System.out.println(matches);
    }
}
