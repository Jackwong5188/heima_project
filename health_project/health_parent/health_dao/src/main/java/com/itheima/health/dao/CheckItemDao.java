package com.itheima.health.dao;

import com.github.pagehelper.Page;
import com.itheima.health.pojo.CheckItem;
import java.util.List;

public interface CheckItemDao {
    List<CheckItem> findAll();

    void add(CheckItem checkItem);

    Page<CheckItem> findPage(String queryString);

    Long findCountByCheckItemId(Integer id);

    void delete(Integer id);

    CheckItem findById(Integer id);

    void edit(CheckItem checkItem);

    //根据检查组d查询关联的多个检查项信息
    List<CheckItem> findCheckItemListById(Integer id);
}
