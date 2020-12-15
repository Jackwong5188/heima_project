package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name="goods")
@RequestMapping(value = "/sku")
public interface SkuFeign {
    /***
     * 根据审核状态查询Sku
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    Result<List<Sku>> findByStatus(@PathVariable(name="status") String status);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable(name="id") Long id);

    /**
     *  给指定的商品的ID减库存
     * @param id  要扣库存的商品的ID skuid
     * @param num  要扣的数量(销售数量)
     * @return
     */
    @GetMapping("/decCount")
    public Result decCount(@RequestParam(name="id") Long id, @RequestParam(name="num") Integer num);
}
