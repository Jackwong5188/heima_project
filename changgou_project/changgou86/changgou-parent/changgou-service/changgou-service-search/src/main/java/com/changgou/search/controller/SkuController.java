package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SkuController {
    @Autowired
    private SkuService skuService;

    /**
     * 导入sku数据到es
     * @return
     */
    @GetMapping("/import")
    public Result search(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }

    /**
     * 关键字搜索
     * @param searchMap  是页面传递过来的参数组合而成的对象JSON
     * @return   根据条件查询的结果返回一个Map
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map searchMap){
        Map resultMap = skuService.search(searchMap);
        return resultMap;
    }
}
