package com.tensquare.article.controller;

import com.tensquare.article.entity.Comment;
import com.tensquare.article.service.CommentService;
import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@CrossOrigin  //解决跨域问题
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisTemplate redisTemplate;

    //新增
    @RequestMapping(method = RequestMethod.POST)
    public Result add(@RequestBody Comment comment) {
        commentService.save(comment);
        return new Result(true, StatusCode.OK, "新增成功");
    }

    //删除评论
   //1. 不删子评论, 只删自己
   //2. 把所有的子评论也删除
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Result deleteById(@PathVariable String id) {
        //不删子评论, 只删自己
        //commentService.deleteById(id);
        //把所有的子评论也删除
        commentService.deleteChildByid(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    //修改评论
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable String id,
                         @RequestBody Comment comment) {
        comment.set_id(id);
        commentService.update(comment);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    //查询所有评论
    @RequestMapping(method = RequestMethod.GET)
    public Result findAll() {
        List<Comment> list = commentService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", list);
    }

    //根据id查询评论
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable String id) {
        Comment comment = commentService.findById(id);
        return new Result(true, StatusCode.OK, "查询成功", comment);
    }

    //根据文章id查询评论
    @RequestMapping(value = "/article/{articleId}", method = RequestMethod.GET)
    public Result findByarticleId(@PathVariable String articleId) {
        List<Comment> list = commentService.findByarticleId(articleId);
        return new Result(true, StatusCode.OK, "查询成功", list);
    }

    //评论点赞
    @RequestMapping(value = "/thumbup/{id}", method = RequestMethod.PUT)
    public Result thumbup(@PathVariable String id) {
        String userId = "123";  //用户id
        //redis中获取点赞信息, key: 评论id+用户id
        Object value = redisTemplate.opsForValue().get(userId + "_"+id);
        if(value!=null){
            return new Result(false, StatusCode.REPERROR, "不能重复点赞");
        }
        commentService.thumbup(id);
        //redis保存点赞信息, key: 评论id+用户id
        redisTemplate.opsForValue().set(userId + "_"+id,"ok");
        return new Result(true, StatusCode.OK, "点赞成功");
    }
}
