package com.itheima.health.test;

import com.itheima.health.utils.MD5Utils;
import org.junit.Test;

public class TestMd5 {
    @Test
    public void run(){
        String s = MD5Utils.md5("123");  //对明文密码123加密
        System.out.println(s);

        String s2 = MD5Utils.md5("123");  //对明文密码123加密
        System.out.println(s2);

        String s3 = MD5Utils.md5("amdin123");  //对明文密码123加密
        System.out.println(s3);

    }
}
