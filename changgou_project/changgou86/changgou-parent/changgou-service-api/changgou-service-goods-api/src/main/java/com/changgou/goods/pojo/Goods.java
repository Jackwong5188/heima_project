package com.changgou.goods.pojo;

import java.io.Serializable;
import java.util.List;

/***
 * 组合对象 组合了SPU 和SKU的列表
 * @author ljh
 * @packagename com.changgou.goods.pojo
 * @version 1.0
 * @date 2020/3/20
 */
public class Goods implements Serializable {
    private Spu spu;//1
    private List<Sku> skuList; //N


    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
