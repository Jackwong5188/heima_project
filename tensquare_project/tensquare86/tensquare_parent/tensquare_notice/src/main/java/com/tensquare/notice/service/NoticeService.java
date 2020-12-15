package com.tensquare.notice.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.tensquare.article.feign.Articlefeign;
import com.tensquare.entity.PageResult;
import com.tensquare.entity.Result;
import com.tensquare.notice.dao.NoticeDao;
import com.tensquare.notice.dao.NoticeFreshDao;
import com.tensquare.notice.pojo.Notice;
import com.tensquare.notice.pojo.NoticeFresh;
import com.tensquare.user.feign.UserFeign;
import com.tensquare.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class NoticeService {
    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private NoticeFreshDao noticeFreshDao;

    @Autowired
    Articlefeign articlefeign;

    @Autowired
    UserFeign userFeign;

    //设置通知信息(进行操作的用户昵称、对象名称)
    private void setNoticeInfo(Notice notice) {
        //通过userFeign中findById()方法获取 userResult
        Result useResult = userFeign.selectById(notice.getOperatorId());
        HashMap userMap = (HashMap) useResult.getData(); //userMap就是用户
        //设置进行操作的用户昵称
        notice.setOperatorName(userMap.get("nickname").toString());

        if ("article".equals(notice.getTargetType())) { //对象类型 等于 文章
            //通过articlefeign中findById()方法获取 articleResult
            Result articleResult = articlefeign.findById(notice.getTargetId());
            HashMap articleMap = (HashMap) articleResult.getData(); //articleMap就是文章
            //设置对象名称
            notice.setTargetName(articleMap.get("title").toString());
        }
    }

    //根据id查询通知
    public Notice findById(String id) {
        Notice notice = noticeDao.selectById(id);
        //设置通知信息(进行操作的用户昵称、对象名称)
        setNoticeInfo(notice);
        return notice;
    }

    //新增通知
    public void add(Notice notice) {
        notice.setState("0");  //消息状态（0 未读，1 已读）
        notice.setCreatetime(new Date());
        String id = idWorker.nextId() + "";
        notice.setId(id);  //设置通知id
        //1.新增通知
        noticeDao.insert(notice);

        //NoticeFresh noticeFresh = new NoticeFresh();
        //noticeFresh.setNoticeId(id);//通知id
        //noticeFresh.setUserId(notice.getReceiverId());//待通知用户的id
        //2.新增待推送通知
        //noticeFreshDao.insert(noticeFresh);
    }

    /**
     * 分页查询通知
     * @param page  当前页码
     * @param size  每页展示数量
     * @return
     */
    @RequestMapping(value = "/findPage/{page}/{size}",method = RequestMethod.GET)
    public PageResult<Notice> findPage(@PathVariable int page, @PathVariable int size) {
        //0.页面结果集对象
        PageResult<Notice> pageResult = new PageResult<>();

        //1.封装分页对象
        Pagination pagination = new Pagination(page, size);

        //2.封装条件对象
        //EntityWrapper<Notice> entityWrapper = new EntityWrapper<>();

        //3.调用Dao
        //根据分页对象、条件对象查询当前页数据
        List<Notice> notices = noticeDao.selectPage(pagination, null);
        //遍历通知
        for (Notice notice : notices) {
            //设置通知信息(进行操作的用户昵称、对象名称)
            setNoticeInfo(notice);
        }
        pageResult.setRows(notices);//设置当前页展示数据

        //根据条件对象查询通知总数
        Integer count = noticeDao.selectCount(null);
        pageResult.setTotal(Long.valueOf(count));  //设置通知总数
        return pageResult;
    }

    //修改通知
    public void update(Notice notice) {
        noticeDao.updateById(notice);
    }

    /**
     * (根据用户id)分页查询待推送通知
     * @param userId   用户id
     * @param page  当前页码
     * @param size  每页展示数量
     * @return
     */
    public PageResult<NoticeFresh> freshPage(String userId, int page, int size) {
        //0.页面结果集对象
        PageResult<NoticeFresh> pageResult = new PageResult<>();

        //1.封装分页对象
        Pagination pagination = new Pagination(page, size);

        //2.封装条件对象
        EntityWrapper<NoticeFresh> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("userId",userId);

        //3.调用Dao
        //根据分页对象、条件对象查询当前页数据
        List<NoticeFresh> noticeFreshes= noticeFreshDao.selectPage(pagination, entityWrapper);
        pageResult.setRows(noticeFreshes);//设置当前页展示数据

        //根据条件对象查询通知总数
        Integer count = noticeFreshDao.selectCount(entityWrapper);
        pageResult.setTotal(Long.valueOf(count));  //设置通知总数
        return pageResult;
    }
}
