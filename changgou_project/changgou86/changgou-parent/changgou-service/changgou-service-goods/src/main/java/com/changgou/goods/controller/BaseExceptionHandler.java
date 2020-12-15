package com.changgou.goods.controller;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice //用来标识类是一个异常处理类
public class BaseExceptionHandler {

    /***
     * 异常处理
     * @param e
     * @return
     */
    //定义一个方法 用于捕获被@resutMapping，postmaping,getmapping修饰的（方法）异常，并统一处理  一旦出现了异常才会执行
    @ExceptionHandler(value = Exception.class)
    @ResponseBody //返回一个JSON
    public Result error(Exception e) {
        e.printStackTrace();
        return new Result(false, StatusCode.ERROR, e.getMessage());
    }
}
