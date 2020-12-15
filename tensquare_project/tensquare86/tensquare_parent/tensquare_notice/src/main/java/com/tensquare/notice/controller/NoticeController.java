package com.tensquare.notice.controller;

import com.tensquare.entity.PageResult;
import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import com.tensquare.notice.pojo.Notice;
import com.tensquare.notice.pojo.NoticeFresh;
import com.tensquare.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
@CrossOrigin //解决跨域问题
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    //根据id查询通知
    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id) {
        Notice notice =  noticeService.findById(id);
        return new Result(true, StatusCode.OK, "查询成功",notice);
    }

    //新增通知
    @RequestMapping(method = RequestMethod.POST)
    public Result add(@RequestBody Notice notice) {
        noticeService.add(notice);
        return new Result(true, StatusCode.OK, "新增成功");
    }

    /**
     * 分页查询通知
     * @param page  当前页码
     * @param size  每页展示数量
     * @return
     */
    @RequestMapping(value = "/findPage/{page}/{size}",method = RequestMethod.GET)
    public Result findPage(@PathVariable int page,@PathVariable int size) {
        PageResult<Notice> pageResult = noticeService.findPage(page,size);
        return new Result(true, StatusCode.OK, "查询成功",pageResult);
    }

    //修改通知
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public Result update(@PathVariable String id,@RequestBody Notice notice) {
        notice.setId(id);
        noticeService.update(notice);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    /**
     * (根据用户id)分页查询待推送通知
     * @param userId   用户id
     * @param page  当前页码
     * @param size  每页展示数量
     * @return
     */
    @RequestMapping(value = "/freshPage/{userId}/{page}/{size}",method = RequestMethod.GET)
    public Result freshPage(@PathVariable String userId,@PathVariable int page ,@PathVariable int size) {
        PageResult<NoticeFresh> pageResult = noticeService.freshPage(userId,page,size);
        return new Result(true, StatusCode.OK, "查询成功",pageResult);
    }
}