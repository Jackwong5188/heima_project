package com.itheima.health.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.health.dao.CheckItemDao;
import com.itheima.health.entity.PageResult;
import com.itheima.health.service.CheckItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.itheima.health.pojo.CheckItem;

import java.util.List;

@Service  // dubbo提供
@Transactional
public class CheckItemServiceImpl implements CheckItemService {

    @Autowired
    CheckItemDao checkItemDao;

    @Override
    public List<CheckItem> findAll() {
        return checkItemDao.findAll();
    }

    @Override
    public void add(CheckItem checkItem) {
        checkItemDao.add(checkItem);
    }

    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
        // 二：使用Mybatis的分页插件，不使用分页的查询，也能完成（即不使用limit），在applicationContext-dao.xml中定义分页插件
        // 1：初始化数据
        PageHelper.startPage(currentPage,pageSize);
        // 2：查询，第一种，返回Page (Page由Mybatis的分页插件提供)
        Page<CheckItem> page = checkItemDao.findPage(queryString);
        // 3：封装PageResult
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void delete(Integer id) throws RuntimeException {
        //查询当前检查项是否和检查组关联
        Long count = checkItemDao.findCountByCheckItemId(id);
        if(count>0){
            //当前检查项被引用，不能删除
            throw new RuntimeException("当前检查项被检查组引用，不能删除");
        }
        checkItemDao.delete(id);
    }

    @Override
    public CheckItem findById(Integer id) {
        CheckItem checkItem = checkItemDao.findById(id);
        return checkItem;
    }

    @Override
    public void edit(CheckItem checkItem) {
        checkItemDao.edit(checkItem);
    }
}
