package com.itheima.health.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.entity.PageResult;
import com.itheima.health.entity.QueryPageBean;
import com.itheima.health.entity.Result;
import com.itheima.health.service.CheckItemService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.itheima.health.pojo.CheckItem;
import java.util.List;

@RestController
@RequestMapping("/checkitem")
public class CheckItemController {
    @Reference  // 订阅 dubbo注解
    CheckItemService checkItemService;

    // 查询所有检查项
    @RequestMapping("/findAll")
    public Result findAll() {
        List<CheckItem> list = checkItemService.findAll();
        if(list!=null && list.size()>0){ //成功
            return new Result(true, MessageConstant.QUERY_CHECKITEM_SUCCESS,list);
        }else { //失败
            return new Result(false, MessageConstant.QUERY_CHECKITEM_FAIL);
        }
    }

    // 分页查询(检查项)
    @RequestMapping("/findPage")
    @PreAuthorize("hasAuthority('CHECKITEM_QUERY')")//权限校验
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        PageResult pageResult = checkItemService.findPage(queryPageBean.getCurrentPage(),
                queryPageBean.getPageSize(),queryPageBean.getQueryString());
        return pageResult;
    }

    // 新增检查项
    @RequestMapping("/add")
    @PreAuthorize("hasAuthority('CHECKITEM_ADD')")//权限校验
    public Result add(@RequestBody CheckItem checkItem) {
        try {
            checkItemService.add(checkItem);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.ADD_CHECKITEM_FAIL);
        }
        return new Result(true,MessageConstant.ADD_CHECKITEM_SUCCESS);
    }

    // 删除检查项
    @RequestMapping("/delete")
    @PreAuthorize("hasAuthority('CHECKITEM_DELETE333')")//权限校验
    public Result delete(Integer id) {
        try {
            checkItemService.delete(id);
        }catch (RuntimeException e){
            return new Result(false,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.DELETE_CHECKITEM_FAIL);
        }
        return new Result(true,MessageConstant.DELETE_CHECKITEM_SUCCESS);
    }

    // 检查项的ID查询(检查项)
    @RequestMapping("/findById")
    public Result findById(Integer id) {
        CheckItem checkItem = checkItemService.findById(id);
        if(checkItem != null){
            return new Result(true,MessageConstant.QUERY_CHECKITEM_SUCCESS,checkItem);
        }else {
            return new Result(false,MessageConstant.QUERY_CHECKITEM_FAIL);
        }
    }

    //编辑(检查项)
    @RequestMapping("/edit")
    @PreAuthorize("hasAuthority('CHECKITEM_EDIT')")//权限校验
    public Result edit(@RequestBody CheckItem checkItem) {
        try {
            checkItemService.edit(checkItem);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.EDIT_CHECKITEM_FAIL);
        }
        return new Result(true,MessageConstant.EDIT_CHECKITEM_SUCCESS);
    }
}
