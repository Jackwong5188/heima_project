package com.tensquare.article.controller;

import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice //通过aop的方式对controller进行增强
public class MyExceptionHandler {

    @ExceptionHandler(Exception.class) //处理Exception异常
    @ResponseBody //返回json格式数据
    public Result handleException(Exception e){
        e.printStackTrace();  //打印异常信息
        return new Result(false, StatusCode.ERROR, e.getMessage());
    }
}
