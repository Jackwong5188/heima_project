package com.itheima.health.dao;

import com.github.pagehelper.Page;
import com.itheima.health.pojo.CheckGroup;
import com.itheima.health.pojo.CheckItem;
import com.itheima.health.pojo.Setmeal;

import java.util.List;
import java.util.Map;

public interface SetmealDao {
    void setSetmealAndCheckGroup(Map<String, Integer> map);

    void add(Setmeal setmeal);

    Page<Setmeal> findPage(String queryString);

    //根据套餐id查询套餐信息
    Setmeal findById(Integer id);

    List<Integer> findCheckGroupIdsBySetmealId(Integer id);

    void deleteAssociation(Integer id);

    void edit(Setmeal setmeal);

    Long findSetmealAndCheckGroupBySetmealId(Integer id);

    void delete(Integer id);

    List<Setmeal> findAll();

    //查找套餐数量
    List<Map<String, Object>> findSetmealCount();
}
