package com.tensquare.article.util;

import com.tensquare.article.entity.Comment;
import org.springframework.data.mongodb.core.query.Update;

import java.lang.reflect.Field;

public class MongoUtils {
    //获取 修改内容update
    public static Update getUpdateByBean(Object object) {
        Update update = new Update();
        //1.通过反射拿到属性
        Field[] declaredFields = object.getClass().getDeclaredFields();
        //2.遍历属性
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);//允许通过get方法直接获取私有属性值
            try {
                //3.属性值不为空作为update条件
                Object value = declaredField.get(object);
                if(value!=null){
                    //属性名就是key
                    String key = declaredField.getName();
                    update = update.set(key,value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        //4.返回update
        return update;
    }
}
