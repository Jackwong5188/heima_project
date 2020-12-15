package com.tensquare.article.feign;

import com.tensquare.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "tensquare-article")
//@RequestMapping("/article")
public interface Articlefeign {
    //根据id查文章
    @RequestMapping(value = "/article/{articleId}",method = RequestMethod.GET)
    public Result findById(@PathVariable("articleId") String articleId);
}
