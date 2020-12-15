package com.changgou.search.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/***
 * 通过spring data elasticsearch的注解来实现 创建索引  和类型 和文档 的唯一标识
 * 通过注解实现手动的映射
 *      + 是否分词     分词的目的就是为了索引  （为了建立倒排索引表）
 *      + 是否索引     索引的目的就是为了搜索
 *      + 是否存储     看页面是否展示  不存储，是否存储指的是存储到lucene中 eS已经存储了一份到_source自Duang中了。
 *      + 数据类型     数据存储的类型是什么
 *@Document(indexName = "skuinfo", type = "docs")
 *  document用于映射到ES的中的文档中
 *     indexName 指定要创建的索引的名称
 *     type 指定要创建的类型
 *
 *
 *    @Field 修饰POJO中的属性 建立和es中的字段的关系
 *  @Field(type = FieldType.Text, analyzer = "ik_smart",index = true,store = false,searchAnalyzer = "ik_smart")
 *      type:指定数据类型
 *      analyzer:指定创建数据到ES中的时候使用的分词器（建立倒排索引的时候适应分词器）
 *      index:是否索引  默认是true
 *      store :是否存储 默认是false
 *      searchAnalyzer：指定的是查询的时候使用的分词器 一般 建立数据和搜索数据的分词器一致
 *
 *
 * @author ljh
 * @packagename com.changgou.search.pojo
 * @version 1.0
 * @date 2020/3/23
 */

@Document(indexName = "skuinfo", type = "docs")
public class SkuInfo implements Serializable {

    @Id//文档的唯一标识 同时也是商品的SKU的ID 主键
    private Long id;

    //SKU名称
    @Field(type = FieldType.Text, analyzer = "ik_smart",index = true,store = false,searchAnalyzer = "ik_smart")
    private String name;

    //商品价格，单位为：元
    @Field(type = FieldType.Double)
    private Long price;

    //库存数量
    private Integer num;

    //商品图片
    //不写的话，当字段有值的时候，会自动的进行映射。
    private String image;

    //商品状态，1-正常，2-下架，3-删除
    private String status;

    //创建时间
    private Date createTime;

    //更新时间
    private Date updateTime;

    //是否默认
    private String isDefault;

    //SPUID
    private Long spuId;

    //类目ID
    private Long categoryId;

    //类目名称
    // FieldType.Keyword 就是字符串，而且一定是不分词的
    @Field(type = FieldType.Keyword)
    private String categoryName;

    //品牌名称
    @Field(type = FieldType.Keyword)
    private String brandName;

    //规格
    private String spec;

    //规格参数
    private Map<String, Object> specMap;






    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public Long getSpuId() {
        return spuId;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Map<String, Object> getSpecMap() {
        return specMap;
    }

    public void setSpecMap(Map<String, Object> specMap) {
        this.specMap = specMap;
    }
}