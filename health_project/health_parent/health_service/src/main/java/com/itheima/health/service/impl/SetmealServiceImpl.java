package com.itheima.health.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.health.constant.RedisConstant;
import com.itheima.health.dao.CheckItemDao;
import com.itheima.health.dao.SetmealDao;
import com.itheima.health.entity.PageResult;
import com.itheima.health.pojo.CheckGroup;
import com.itheima.health.pojo.CheckItem;
import com.itheima.health.pojo.Setmeal;
import com.itheima.health.service.CheckItemService;
import com.itheima.health.service.SetmealService;
import com.itheima.health.utils.QiniuUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service  // dubbo提供
@Transactional
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealDao setmealDao;

    @Autowired
    JedisPool jedisPool;

    @Autowired   // 注入FreeMarkerConfigurer
    FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${out_put_path}") //从属性文件读取输出目录的路径
    String outputpath;

    //添加套餐，同时需要设置套餐和检查组的关联关系
    @Override
    public void add(Setmeal setmeal, Integer[] checkgroupIds) {
        setmealDao.add(setmeal);
        //设置套餐和检查组的多对多关系
        setSetmealAndCheckGroup(setmeal.getId(),checkgroupIds);
        //将图片名称保存到Redis
        jedisPool.getResource().sadd(RedisConstant.SETMEAL_PIC_DB_RESOURCE,setmeal.getImg());

        //新增套餐后需要重新生成静态页面
        generateMobileStaticHtml();
    }

    //生成静态页面
    public void generateMobileStaticHtml() {
        //准备模板文件中所需的数据
        List<Setmeal> setmealList = this.findAll();
        //生成套餐列表静态页面
        generateMobileSetmealListHtml(setmealList);
        //生成套餐详情静态页面（多个）
        generateMobileSetmealDetailHtml(setmealList);
    }

    //生成套餐列表静态页面
    public void generateMobileSetmealListHtml(List<Setmeal> setmealList) {
        Map<String,Object> map = new HashMap<>();
        map.put("setmealList",setmealList);
        // 生成静态页面（参数1：静态页面的ftl文件名，参数2：静态页面的名称，参数三：map）
        this.generateHtml("mobile_setmeal.ftl","m_setmeal.html",map);
    }

    //生成套餐详情静态页面（多个）
    public void generateMobileSetmealDetailHtml(List<Setmeal> setmealList) {
        for (Setmeal setmeal : setmealList) {
            Map<String,Object> map = new HashMap<>();
            map.put("setmeal",this.findById(setmeal.getId()));
            // 生成静态页面（参数1：静态页面的ftl文件名，参数2：静态页面的名称，参数三：map）
            this.generateHtml("mobile_setmeal_detail.ftl","setmeal_detail_"+setmeal.getId()+".html",map);
        }
    }

    // 生成静态页面（参数1：静态页面的ftl文件名，参数2：静态页面的名称，参数三：map）
    public void generateHtml(String templateName,String htmlPageName,Map<String,Object> map) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        Writer out = null;
        try {
            // 加载模版文件
            Template template = configuration.getTemplate(templateName);
            // 生成数据
            File docFile = new File(outputpath + "\\" + htmlPageName);
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(docFile)));
            // 输出文件
            template.process(map,out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null){
                    out.flush();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
        // 二：使用Mybatis的分页插件，不使用分页的查询，也能完成（即不使用limit），在applicationContext-dao.xml中定义分页插件
        // 1：初始化数据
        PageHelper.startPage(currentPage,pageSize);
        // 2：查询，第一种，返回Page (Page由Mybatis的分页插件提供)
        Page<Setmeal> page = setmealDao.findPage(queryString);
        // 3：封装PageResult
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public Setmeal findById(Integer id) {
        return setmealDao.findById(id);
    }

    @Override
    public List<Integer> findCheckGroupIdsBySetmealId(Integer id) {
        return setmealDao.findCheckGroupIdsBySetmealId(id);
    }

    //编辑套餐，同时需要更新和检查组的关联关系
    @Override
    public void edit(Setmeal setmeal, Integer[] checkgroupIds) {
        // 使用套餐id，查询数据库对应的套餐，获取数据库存放的img
        Setmeal setmeal_db = setmealDao.findById(setmeal.getId());
        String img = setmeal_db.getImg();
        // 如果页面传递的图片名称和数据库存放的图片名称不一致，说明图片更新，需要删除七牛云之前数据库的图片
        if(setmeal.getImg() != null && !setmeal.getImg().equals(img)){
            //删除七牛云之前数据库的图片
            QiniuUtils.deleteFileFromQiniu(img);
            //将图片名称从Redis中删除，key值为setmealPicDbResources
            jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_DB_RESOURCE,img);
            //将图片名称从Redis中删除，key值为setmealPicResources
            jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_RESOURCE,img);
            // 将页面更新的图片，存放到key值为SETMEAL_PIC_DB_RESOURCES的redis中
            jedisPool.getResource().sadd(RedisConstant.SETMEAL_PIC_DB_RESOURCE,setmeal.getImg());
        }

        //1：根据套餐id删除中间表数据（清理原有关联关系）
        setmealDao.deleteAssociation(setmeal.getId());
        //2：向中间表(t_setmeal_checkgroup)插入数据（建立套餐和检查组关联关系）
        addSetmealAndCheckGroup(setmeal.getId(),checkgroupIds);
        //3：更新套餐基本信息
        setmealDao.edit(setmeal);

        // 重新使用Freemarker生成静态页面
        generateMobileStaticHtml();
    }

    // 删除套餐
    @Override
    public void delete(Integer id) throws RuntimeException {
        // 使用套餐id，查询数据库对应的套餐，获取数据库存放的img
        Setmeal setmeal_db = setmealDao.findById(id);
        String img = setmeal_db.getImg();
        // 需要先删除七牛云之前数据库的图片
        if(img != null && !"".equals(img)){
            //删除七牛云之前数据库的图片
            QiniuUtils.deleteFileFromQiniu(img);
            //将图片名称从Redis中删除，key值为setmealPicDbResources
            jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_DB_RESOURCE,img);
            //将图片名称从Redis中删除，key值为setmealPicResources
            jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_RESOURCE,img);
        }

        //使用套餐id，查询套餐和检查组中间表
        Long count = setmealDao.findSetmealAndCheckGroupBySetmealId(id);
        if(count>0){
            throw new RuntimeException("当前套餐和检查组之间存在关联关系，不能删除");
        }
        setmealDao.delete(id);

        // 重新使用Freemarker生成静态页面
        generateMobileStaticHtml();
    }

    @Override
    public List<Setmeal> findAll() {
        return setmealDao.findAll();
    }

    //查找套餐数量
    @Override
    public List<Map<String, Object>> findSetmealCount() {
        return setmealDao.findSetmealCount();
    }

    //向中间表(t_setmeal_checkgroup)插入数据（建立检查组和检查项关联关系）
    public void addSetmealAndCheckGroup(Integer setmealId,Integer[] checkgroupIds){
        if(checkgroupIds != null && checkgroupIds.length>0){
            for (Integer checkgroupId : checkgroupIds) {
                Map<String,Integer> map = new HashMap<>();
                map.put("setmeal_id",setmealId);
                map.put("checkgroup_id",checkgroupId);
                setmealDao.setSetmealAndCheckGroup(map);
            }
        }
    }

    //设置套餐和检查组的多对多关系
    private void setSetmealAndCheckGroup(Integer id, Integer[] checkgroupIds) {
        if(checkgroupIds != null && checkgroupIds.length>0){
            for (Integer checkgroupId : checkgroupIds) {
                Map<String, Integer> map = new HashMap<>();
                map.put("checkgroup_id",checkgroupId);
                map.put("setmeal_id",id);
                setmealDao.setSetmealAndCheckGroup(map);
            }
        }
    }
}
