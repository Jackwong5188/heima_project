package com.tensquare.article.service;

import com.tensquare.article.entity.Comment;
import com.tensquare.article.mongodao.CommentDao;
import com.tensquare.article.util.MongoUtils;
import com.tensquare.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private MongoTemplate mongoTemplate;

    //新增
    public void save(Comment comment) {
        String id = idWorker.nextId() + "";
        comment.set_id(id);  //设置主键id

        comment.setPublishdate(new Date()); //设置发布日期
        comment.setThumbup(0);  //设置点赞数
        //新增
        commentDao.save(comment);
    }

    //删除(不删子评论, 只删自己 )
    public void deleteById(String id) {
        commentDao.deleteById(id);
    }

    //删除(把所有的子评论也删除)
    public void deleteChildByid(String commentId){
        //1.查出当前评论的子评论
        List<Comment> childList = commentDao.findByParentid(commentId);
        //2.删除当前评论
        commentDao.deleteById(commentId);
        //3.遍历子评论递归删除
        for (Comment comment : childList) {
            deleteById(comment.get_id());
        }
    }

    //修改评论
    public void update(Comment comment) {
        //1.查询条件
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(comment.get_id()));
        //2.修改内容
        //Update update = new Update();
        //update.set("content",comment.getContent()).set("thumbup",comment.getThumbup());
        Update update = MongoUtils.getUpdateByBean(comment);
        //3.集合名称
        mongoTemplate.updateFirst(query,update,"comment");
    }

    //查询所有评论
    public List<Comment> findAll() {
        List<Comment> comments = commentDao.findAll();
        return comments;
    }

    //根据id查询评论
    public Comment findById(String id) {
        Comment comment = commentDao.findById(id).get();
        return comment;
    }

    //根据文章id查询评论
    public List<Comment> findByarticleId(String articleId) {
        List<Comment> comments = commentDao.findByArticleid(articleId);
        return comments;
    }

    //评论点赞
    public void thumbup(String id) {
        //1.查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        //2.修改内容
        Update update = new Update();
        update.inc("thumbup",1);
        //3.集合名称
        mongoTemplate.updateFirst(query,update,"comment");
    }
}
