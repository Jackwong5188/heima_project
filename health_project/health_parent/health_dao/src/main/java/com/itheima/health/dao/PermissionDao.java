package com.itheima.health.dao;

import com.itheima.health.pojo.Permission;

import java.util.Set;

public interface PermissionDao {
    //根据角色id查询(多个)权限
    Set<Permission> findPermissionsByRoleId(Integer roleId);
}
