package com.tensquare.notice.feign;

import com.tensquare.entity.Result;
import com.tensquare.notice.pojo.Notice;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "tensquare-notice")
//@RequestMapping("/notice")
public interface NoticeFeign {
    //新增通知
    @RequestMapping(value = "/notice",method = RequestMethod.POST)
    public Result add(@RequestBody Notice notice);
}
