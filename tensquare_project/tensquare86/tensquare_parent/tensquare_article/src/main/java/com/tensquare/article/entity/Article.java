package com.tensquare.article.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tb_article")//表名,实体类的属性最好是包装类，包装类可以为null，在修改数据库的时候，
// 实体类属性为null，不会覆盖原本的值
public class Article implements Serializable {

    @TableId(type = IdType.INPUT)//用户输入
    private String id;//ID
    //@TableField("columnid")//如果数据库字段和属性名不一致可以使用该注解
    private String columnid;    //专栏ID
    private String userid;      //用户ID
    private String title;       //标题
    private String content;     //文章正文
    private String image;       //文章封面
    private Date createtime;    //发表日期
    private Date updatetime;    //修改日期
    private String ispublic;    //是否公开
    private String istop;       //是否置顶
    private Integer visits;     //浏览量
    private Integer thumbup;    //点赞数
    private Integer comment;    //评论数
    private String state;       //审核状态
    private String channelid;   //所属频道
    private String url;         //URL
    private String type;        //类型
}