package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="user")
@RequestMapping("/user")
public interface UserFeign {
    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping("/load/{id}")
    Result<User> loadById(@PathVariable(name="id") String id);

    /**
     * 给指定的用户名 添加积分
     * @param username  用户名
     * @param points  积分数
     * @return
     */
    @GetMapping("/points/add")
    public Result addPoints(@RequestParam(name="username") String username,
                            @RequestParam(name="points") Integer points);
}
