package com.tensquare.notice.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Notice implements Serializable {

    private String id;//ID

    private String receiverId;//接收消息的用户ID
    private String operatorId;//进行操作的用户ID

    private String operatorName;//进行操作的用户昵称
    private String action;//操作类型（评论，点赞等）
    private String targetType;//被操作的对象类型，例如文章，评论等

    private String targetName;//对象名称或简介
    private String targetId;//被操作对象的id，例如文章的id，评论的id
    private Date createtime;//创建日期
    private String type;	//通知类型
    private String state;	//通知状态（0 未读，1 已读）

}
