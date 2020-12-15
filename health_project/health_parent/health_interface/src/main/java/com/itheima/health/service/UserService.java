package com.itheima.health.service;

import com.itheima.health.pojo.User;

public interface UserService {
    //根据用户名查询用户信息
    User findUserByUsername(String username);
}
