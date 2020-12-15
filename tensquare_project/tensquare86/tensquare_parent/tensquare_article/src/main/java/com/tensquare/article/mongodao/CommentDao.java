package com.tensquare.article.mongodao;

import com.tensquare.article.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentDao extends MongoRepository<Comment,String> {
    //1.查出当前评论的子评论
    List<Comment> findByParentid(String commentId);

    //2. 根据文章id查询评论
    List<Comment> findByArticleid(String articleId);
}
