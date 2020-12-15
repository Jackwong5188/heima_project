package com.itheima.health.dao;

import com.github.pagehelper.Page;
import com.itheima.health.pojo.CheckGroup;

import java.util.List;
import java.util.Map;

public interface CheckGroupDao {
    void add(CheckGroup checkGroup);

    void setCheckGroupAndCheckItem(Map<String, Integer> map);

    Page<CheckGroup> findPage(String queryString);

    CheckGroup findById(Integer id);

    List<CheckGroup> findAll();

    List<Integer> findCheckItemIdsByCheckGroupId(Integer id);

    void deleteAssociation(Integer id);

    void edit(CheckGroup checkGroup);

    Long findCheckGroupAndCheckItemByCheckGroupId(Integer id);

    Long findSetmealAndCheckGroupByCheckGroupId(Integer id);

    void delete(Integer id);

    //根据套餐id查询关联的多个检查组信息
    List<CheckGroup> findCheckGroupListById(Integer id);
}
