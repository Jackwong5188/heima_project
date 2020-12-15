package com.changgou.goods.dao;
import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:admin
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {
    /**
     *  给指定的商品的ID减库存
     * @param id  要扣库存的商品的ID skuid
     * @param num  要扣的数量(销售数量)
     * @return
     */
    @Update(value = "update tb_sku set num=num-#{num}, sale_num=sale_num+#{num} where id=#{id} and num>=#{num}")
    int decCount(@Param(value="id")Long id, @Param(value="num")Integer num);
}
