package com.itheima.health.service;

import com.itheima.health.entity.PageResult;
import com.itheima.health.pojo.CheckItem;

import java.util.List;

public interface CheckItemService {
    List<CheckItem> findAll();

    void add(CheckItem checkItem);

    PageResult findPage(Integer currentPage, Integer pageSize, String queryString);

    void delete(Integer id) throws RuntimeException;

    CheckItem findById(Integer id);

    void edit(CheckItem checkItem);
}
