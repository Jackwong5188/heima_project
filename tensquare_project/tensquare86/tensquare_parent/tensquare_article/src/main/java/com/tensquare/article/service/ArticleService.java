package com.tensquare.article.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.tensquare.article.conf.RabbitmqConfig;
import com.tensquare.article.dao.ArticleDao;
import com.tensquare.article.entity.Article;
import com.tensquare.entity.PageResult;
import com.tensquare.notice.feign.NoticeFeign;
import com.tensquare.notice.pojo.Notice;
import com.tensquare.util.IdWorker;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
@Service
public class ArticleService {
    @Autowired
    private ArticleDao articleDao;

    @Autowired
    IdWorker idWorker;

    @Autowired
    private NoticeFeign noticeFeign;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 查询所有
     * @return
     */
    public List<Article> findAll() {
        List<Article> articles = articleDao.selectList(null);
        return articles;
    }

    //新增文章
    public void add(Article article) {
        //1.新增文章
        String authorId = "1";
        article.setCreatetime(new Date());  //创建时间
        article.setId(idWorker.nextId() + ""); //文章id
        article.setVisits(0);   //浏览量
        article.setThumbup(0);  //点赞数
        article.setComment(0);  //评论数
        article.setState("0");  //未审核
        article.setUserid(authorId);   //用户ID

        //2.通知
        //2.1 查询当前作者id对应key键在redis中对应的集合
        Set<String> members = redisTemplate.opsForSet().members("article_author_" + authorId);
        //2.2遍历集合，发送通知给集合中的成员
        for (String uid : members) {
            Notice notice = new Notice();
            notice.setReceiverId(uid);  //接收消息用户的ID
            notice.setOperatorId(authorId);  //进行操作用户的ID
            notice.setAction("publish"); //操作类型（评论，点赞等）
            notice.setTargetType("article"); //被操作的对象类型，例如文章，评论等
            notice.setTargetId(article.getId()); //被操作对象的id，例如文章的id，评论的id
            notice.setCreatetime(new Date());
            notice.setType("sys");  //通知类型
            notice.setState("0"); //消息状态（0 未读，1 已读）
            //新增通知
            noticeFeign.add(notice);
        }

        //新增文章
        articleDao.insert(article);

        //3.发通知消息到mq
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ARTICLE,authorId,article.getId());
    }

    //根据id查文章
    public Article findById(String articleId) {
        Article article = articleDao.selectById(articleId);
        return article;
    }

    //更新文章
    public void update(Article article) {
        articleDao.updateById(article);
    }


    //根据id删除文章
    public void delete(String id) {
        articleDao.deleteById(id);
    }

    /**
     * 条件+分页查询
     * @param map   查询参数用map封装
     * @param page  当前页码
     * @param size  每页展示数量
     * @return
     */
    public PageResult<Article> search(Map map, int page, int size) {
        //0.页面结果集对象
        PageResult<Article> pageResult = new PageResult<>();

        //1.封装分页对象
        Pagination pagination = new Pagination(page, size);

        //2.封装条件对象
        EntityWrapper<Article> entityWrapper = new EntityWrapper<>();
        if(!StringUtils.isEmpty(map.get("columnid"))){ //专栏ID
            entityWrapper.eq("columnid",map.get("columnid"));
        }
        if(!StringUtils.isEmpty(map.get("userid"))){   //用户ID
            entityWrapper.eq("userid",map.get("userid"));
        }
        if(!StringUtils.isEmpty(map.get("title"))){    //标题
            entityWrapper.like("title","%"+map.get("title")+"%");
        }
        if(!StringUtils.isEmpty(map.get("content"))){  //正文
            entityWrapper.like("content","%"+map.get("content")+"%");
        }

        //3.调用Dao
        //根据分页对象、条件对象查询当前页数据
        List<Article> articles = articleDao.selectPage(pagination, entityWrapper);
        pageResult.setRows(articles);//设置当前页展示数据

        //根据条件对象查询文章总数
        Integer count = articleDao.selectCount(entityWrapper);
        pageResult.setTotal(Long.valueOf(count));  //设置文章总数
        return pageResult;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DirectExchange exchange;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    /**
     * 订阅(用户根据文章id订阅文章作者)
     *
     * @param articleId
     * @return
     */
    public boolean subscribe(String userId, String articleId) {
        //1.根据文章Id获得文章作者Id
        Article article = articleDao.selectById(articleId);
        String authorId = article.getUserid();
        //2.存到Redis
        //当前用户关注的作者set
        String userKey = "article_subscribe_"+userId;
        //该作者被关注的用户set
        String authorKey = "article_author_"+authorId;

        Boolean isMember = redisTemplate.opsForSet().isMember(userKey, authorId);

        //创建queue
        Queue queue = new Queue("article_subscribe_" + userId, true);
        //声明exchange和queue的绑定关系，设置路由键为作者id
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(authorId);

        //3.判断是否已经订阅
        if(isMember){
            redisTemplate.opsForSet().remove(userKey,authorId);
            redisTemplate.opsForSet().remove(authorKey,userId);
            //进行解绑
            rabbitAdmin.removeBinding(binding);
            return false;
        }else {
            redisTemplate.opsForSet().add(userKey,authorId);
            redisTemplate.opsForSet().add(authorKey,userId);
            //进行绑定
            rabbitAdmin.declareQueue(queue);
            rabbitAdmin.declareBinding(binding);
            return true;
        }
    }

    /**
     * 文章点赞
     * @param articleId
     */
    public void thumb(String articleId, String userId) {
        //1.(根据文章id)获取文章
        Article article = articleDao.selectById(articleId);
        //2. 点赞数+1
        article.setThumbup(article.getThumbup()+1);
        //3. 更新文章
        articleDao.updateById(article);

        //通知消息
        Notice notice = new Notice();
        notice.setReceiverId(article.getUserid()); //接收消息用户的ID
        notice.setOperatorId(userId);  //进行操作用户的ID
        notice.setAction("thumbup");  //操作类型（评论，点赞等）
        notice.setTargetType("article");  //被操作的对象类型，例如文章，评论等
        notice.setTargetId(articleId);  //被操作对象的id，例如文章的id，评论的id
        notice.setCreatetime(new Date());
        notice.setType("user");  //通知类型
        notice.setState("0");  //消息状态（0 未读，1 已读）
        //新增通知
        noticeFeign.add(notice);
    }
}
