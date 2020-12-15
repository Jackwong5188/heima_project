package com.tensquare.article.entity;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

//@Document("comment") 如果类名和集合名一样不需要加该注解
public class Comment implements Serializable {
    @Id  //标识主键
    private String _id;  //主键id
    private String articleid;  //文章id
    private String content;  //内容
    private String userid;   //用户id
    private String parentid; //父类id
    private Date publishdate; //发布日期
    private Integer thumbup;  //点赞数

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getArticleid() {
        return articleid;
    }

    public void setArticleid(String articleid) {
        this.articleid = articleid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public Date getPublishdate() {
        return publishdate;
    }

    public void setPublishdate(Date publishdate) {
        this.publishdate = publishdate;
    }

    public Integer getThumbup() {
        return thumbup;
    }

    public void setThumbup(Integer thumbup) {
        this.thumbup = thumbup;
    }
}