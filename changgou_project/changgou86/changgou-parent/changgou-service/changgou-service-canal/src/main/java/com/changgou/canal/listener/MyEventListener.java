package com.changgou.canal.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.xpand.starter.canal.annotation.*;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/***
 * 描述
 * @author ljh
 * @packagename com.changgou.canal.listener
 * @version 1.0
 * @date 2020/3/22
 */
@CanalEventListener  //修饰类，mysql当的CUD发生的时候由该类的方法来处理
public class MyEventListener {

   /* @InsertListenPoint    当发生INSERT操作的时候触发以下的方法执行
    public void onEvent(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //do something...
    }*/

    /*@UpdateListenPoint(destination = "exmaple",schema = "changgou_content",table = "tb_content")// 当发生UPDATE操作传的时候触发一下方法执行
    public void onEvent1(CanalEntry.RowData rowData) {
        //1.获取更新之前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();

        System.out.println("==============修改之前的数据======");
        for (CanalEntry.Column column : beforeColumnsList) {
            System.out.println(column.getName()+":值是为："+column.getValue());
        }
        //2.获取更新之后的数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        System.out.println("==============修改之后的数据======");
        for (CanalEntry.Column column : afterColumnsList) {
            System.out.println(column.getName()+":值是为："+column.getValue());
        }

    }*/

   /* @DeleteListenPoint
    public void onEvent3(CanalEntry.EventType eventType) {
        //do something...
    }
    */

   @Autowired
   private ContentFeign contentFeign;

   @Autowired
   private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 自定义监听类型
     * destination ：指定的是目的地 和之前的canal-server中的exmpale保持一致
     * schema :指定的是监听哪一个数据库
     * table：指定是监听哪一些张表
     * eventType：当方式了什么样的事件类型 才执行以下的方法的逻辑
     * @param eventType
     * @param rowData
     */
    @ListenPoint(destination = "example", schema = "changgou_content", table = {"tb_content"}, eventType = {
            CanalEntry.EventType.UPDATE,
            CanalEntry.EventType.INSERT,
            CanalEntry.EventType.DELETE})
    public void onEvent4(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //1.判断 操作的类型 如果是更新 或者insert  获取更新后，或者是插入后的数据  获取被修改的数据所属的广告分类的ID

        //2.判断 操作的类型 删除 获取删除之前的数据

        //1.获取列名 为category_id的值
        String categoryId = getColumnValue(eventType, rowData);
        //2.调用feign 获取该分类下的所有的广告集合
        Result<List<Content>> categoryresut = contentFeign.findByCategory(Long.valueOf(categoryId));
        List<Content> data = categoryresut.getData();
        //3.使用redisTemplate存储到redis中
        stringRedisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(data));
    }

    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        String categoryId = "";
        //判断 如果是删除  则获取beforlist
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        } else {
            //判断 如果是添加 或者是更新 获取afterlist
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }
        return categoryId;
    }
}