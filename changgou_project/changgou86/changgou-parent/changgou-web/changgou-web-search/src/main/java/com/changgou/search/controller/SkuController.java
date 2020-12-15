package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SkuController {
    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     * @param searchMap  {keywords:"手机","category":"笔记本"........}
     * @return  逻辑视图  但是model中会有数据
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model){
        //调用changgou-service-search微服务
        Map resultMap = skuFeign.search(searchMap);
        //搜索数据结果
        model.addAttribute("result",resultMap);
        //搜索条件
        model.addAttribute("searchMap",searchMap);
        //请求地址
        String url = url(searchMap);
        model.addAttribute("url",url);
        //分页计算
        Page<SkuInfo> page = new Page<SkuInfo>(
                Long.valueOf(resultMap.get("total").toString()),//总记录数
                Integer.parseInt(resultMap.get("pageNum").toString()),//用户点击的页码
                Integer.parseInt(resultMap.get("pageSize").toString())//每页显示的行
        );
        model.addAttribute("page",page);
        return "search";
    }

    //URL组装和处理
    public String url(Map<String, String> searchMap){
        //URL地址
        String url = "/search/list";
        if(searchMap!=null && searchMap.size()>0){
            url += "?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //跳过分页, 分页参数不拼接
                if(entry.getKey().equals("pageNum")){
                    continue;
                }
                String key = entry.getKey();
                String value = entry.getValue();
                url += key+"="+value + "&";
            }
            //求掉最后一个&
            url = url.substring(0,url.length()-1);
        }
        return url;
    }
}
