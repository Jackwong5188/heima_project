package com.itheima.health.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.health.dao.CheckGroupDao;
import com.itheima.health.entity.PageResult;
import com.itheima.health.pojo.CheckGroup;
import com.itheima.health.pojo.CheckItem;
import com.itheima.health.service.CheckGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service  // dubbo提供
@Transactional
public class CheckGroupServiceImpl implements CheckGroupService {
    @Autowired
    CheckGroupDao checkGroupDao;

    //添加检查组合，同时需要设置检查组合和检查项的关联关系
    @Override
    public void add(CheckGroup checkGroup, Integer[] checkitemIds) {
        checkGroupDao.add(checkGroup);
        setCheckGroupAndCheckItem(checkGroup.getId(),checkitemIds);
    }

    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
        // 二：使用Mybatis的分页插件，不使用分页的查询，也能完成（即不使用limit），在applicationContext-dao.xml中定义分页插件
        // 1：初始化数据
        PageHelper.startPage(currentPage,pageSize);
        // 2：查询，第一种，返回Page (Page由Mybatis的分页插件提供)
        Page<CheckGroup> page = checkGroupDao.findPage(queryString);
        // 3：封装PageResult
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public CheckGroup findById(Integer id) {
        return checkGroupDao.findById(id);
    }

    @Override
    public List<CheckGroup> findAll() {
        return checkGroupDao.findAll();
    }

    @Override
    public List<Integer> findCheckItemIdsByCheckGroupId(Integer id) {
        return checkGroupDao.findCheckItemIdsByCheckGroupId(id);
    }

    //设置检查组合和检查项的关联关系，向中间表中添加数据
    public void setCheckGroupAndCheckItem(Integer checkGroupId,Integer[] checkitemIds) {
        if(checkitemIds != null && checkitemIds.length>0){
            for (Integer checkitemId : checkitemIds) {
                Map<String, Integer> map = new HashMap<>();
                map.put("checkgroup_id",checkGroupId);
                map.put("checkitem_id",checkitemId);
                checkGroupDao.setCheckGroupAndCheckItem(map);
            }
        }
    }

    //编辑检查组，同时需要更新和检查项的关联关系
    @Override
    public void edit(CheckGroup checkGroup, Integer[] checkitemIds) {
        //1：根据检查组id删除中间表数据（清理原有关联关系）
        checkGroupDao.deleteAssociation(checkGroup.getId());
        //2：向中间表(t_checkgroup_checkitem)插入数据（建立检查组和检查项关联关系）
        addCheckGroupAndCheckItem(checkGroup.getId(),checkitemIds);
        //3：更新检查组基本信息
        checkGroupDao.edit(checkGroup);
    }

    // 删除检查组
    @Override
    public void delete(Integer id) throws RuntimeException {
        //使用检查组id，查询检查组和检查项中间表
        Long count1 = checkGroupDao.findCheckGroupAndCheckItemByCheckGroupId(id);
        if(count1>0){
            //当前检查项被引用，不能删除
            throw new RuntimeException("当前检查组和检查项之间存在关联关系，不能删除");
        }
        // 使用检查组id，查询套餐和检查组中间表
        Long count2 = checkGroupDao.findSetmealAndCheckGroupByCheckGroupId(id);
        if(count2>0){
            //当前检查项被引用，不能删除
            throw new RuntimeException("当前套餐和检查组之间存在关联关系，不能删除");
        }
        checkGroupDao.delete(id);
    }

    //向中间表(t_checkgroup_checkitem)插入数据（建立检查组和检查项关联关系）
    public void addCheckGroupAndCheckItem(Integer checkGroupId,Integer[] checkitemIds){
        if(checkitemIds != null && checkitemIds.length>0){
            for (Integer checkitemId : checkitemIds) {
                Map<String,Integer> map = new HashMap<>();
                map.put("checkgroup_id",checkGroupId);
                map.put("checkitem_id",checkitemId);
                checkGroupDao.setCheckGroupAndCheckItem(map);
            }
        }
    }
}
