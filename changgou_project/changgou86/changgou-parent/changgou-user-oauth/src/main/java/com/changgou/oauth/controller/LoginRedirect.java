package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/oauth")
public class LoginRedirect {
    /***
     * 跳转到登录页面
     * @return
     */
    @GetMapping(value = "/login")
    public String login(@RequestParam(value = "FROM",required = false,defaultValue = "")String from, Model model){
        //记录 原访问页 , 如： http://localhost:8001/api/user
        //获取FROM参数，并将参数存储到 model 中
        model.addAttribute("from",from);
        return "login";
    }
}
