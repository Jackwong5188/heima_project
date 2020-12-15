package com.tensquare.article.controller;

import com.tensquare.article.entity.Article;
import com.tensquare.article.service.ArticleService;
import com.tensquare.entity.PageResult;
import com.tensquare.entity.Result;
import com.tensquare.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin//解决跨域问题
@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 文章点赞
     * @param articleId
     * @return
     */
    @RequestMapping(value = "/thumbup/{articleId}", method = RequestMethod.PUT)
    public Result thumbup(@PathVariable(value = "articleId") String articleId) {
        //模拟当前用户
        String userId = "5";

        //控制重复点赞(从redis中获取数据)
        Object value = redisTemplate.opsForValue().get("article_" + userId + "_" + articleId);
        if(value != null){
            return new Result(false, StatusCode.REPERROR, "不能重复点赞");
        }

        //执行点赞
        articleService.thumb(articleId,userId);
        //往redis中存储数据
        redisTemplate.opsForValue().set("article_" + userId + "_" + articleId,"OK");
        return new Result(true, StatusCode.OK, "点赞成功");
    }

    /**
     * 订阅(用户根据文章id订阅文章作者)
     *
     * @param articleId
     * @return
     */
    @RequestMapping(value = "/subscribe/{articleId}", method = RequestMethod.POST)
    public Result subscribe(@PathVariable("articleId") String articleId) {
        String userId = "2"; //用户id
        boolean flag = articleService.subscribe(userId, articleId);
        if(flag){
            return new Result(true, StatusCode.OK, "订阅成功");
        }else{
            return new Result(true, StatusCode.OK, "取消订阅");
        }
    }
    /**
     * 条件+分页查询
     * @param map   查询参数用map封装
     * @param page  当前页码
     * @param size  每页展示数量
     * @return
     */
    @RequestMapping(value = "/search/{page}/{size}",method = RequestMethod.POST)
    public Result search(@RequestBody Map map, @PathVariable(value = "page") int page, @PathVariable(value = "size")int size) {
        PageResult<Article> pageResult = articleService.search(map,page,size);
        return new Result(true, StatusCode.OK, "条件+分页查询成功",pageResult);
    }
    //根据id删除文章
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public Result delete(@PathVariable String id) {
        articleService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    //更新文章
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable String id, @RequestBody Article article) {
        article.setId(id);
        articleService.update(article);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    //根据id查文章
    @RequestMapping(value = "/{articleId}",method = RequestMethod.GET)
    public Result findById(@PathVariable("articleId") String articleId) {
        Article article = articleService.findById(articleId);
        return new Result(true, StatusCode.OK, "文章查询成功", article);
    }

    //新增文章
    @RequestMapping(method = RequestMethod.POST)
    public Result add(@RequestBody Article article) {
        articleService.add(article);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    /**
     * 查询所有文章
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public Result findAll() {
        List<Article> list = articleService.findAll();
        return new Result(true, StatusCode.OK, "所有文章查询成功", list);
    }
}
