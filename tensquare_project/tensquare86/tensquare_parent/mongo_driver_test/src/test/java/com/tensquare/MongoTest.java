package com.tensquare;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class MongoTest {
    MongoCollection<Document> comment;
    MongoClient client;

    @Before
    public void init() {
        //连接客户端
        client = new MongoClient("192.168.211.130");
        //连接数据库
        MongoDatabase commentdb = client.getDatabase("commentdb");
        //获取集合
        comment = commentdb.getCollection("comment");
    }

    //查询所有
    @Test
    public void run(){
        //查询
        FindIterable<Document> documents = comment.find();

        //查询记录获取文档集合
        for (Document document : documents) {
            System.out.println("_id：" + document.get("_id"));
            System.out.println("内容：" + document.get("content"));
            System.out.println("用户ID:" + document.get("userid"));
            System.out.println("点赞数：" + document.get("thumbup"));
        }
    }

    //根据_id查询
    @Test
    public void test2() {
        BasicDBObject basicDBObject = new BasicDBObject("_id", "2");
        //获取文档集合
        FindIterable<Document> documents = comment.find(basicDBObject);

        //遍历
        for (Document document : documents) {
            System.out.println("_id：" + document.get("_id"));
            System.out.println("内容：" + document.get("content"));
            System.out.println("用户ID:" + document.get("userid"));
            System.out.println("点赞数：" + document.get("thumbup"));
        }
    }

    //新增
    @Test
    public void test3() {
        Map<String, Object> map = new HashMap();
        map.put("_id", "7");
        map.put("content", "很棒！");
        map.put("userid", "9999");
        map.put("thumbup", 123);

        Document document = new Document(map);

        comment.insertOne(document);
    }

    //修改
    @Test
    public void test4() {
        //修改的条件
        Bson filter = new BasicDBObject("_id", "7");
        //修改的数据
        Bson update = new BasicDBObject("$set", new Document("userid", "8888"));

        comment.updateOne(filter, update);
    }

    //删除
    @Test
    public void test5() {
        //删除的条件
        Bson filter = new BasicDBObject("_id", "7");

        comment.deleteOne(filter);
    }

    @After
    public void after() {
        //关闭连接
        client.close();
    }
}
