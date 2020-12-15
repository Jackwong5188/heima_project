package com.itheima.health.dao;

import com.itheima.health.pojo.Role;

import java.util.Set;

public interface RoleDao {
    //根据用户id查询(多个)角色
    Set<Role> findRolesByUserId(Integer userId);
}
