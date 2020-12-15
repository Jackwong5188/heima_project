package com.itheima.health.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.health.dao.OrderSettingDao;
import com.itheima.health.pojo.OrderSetting;
import com.itheima.health.service.OrderSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 预约设置
 */
@Service
@Transactional
public class OrderSettingServiceImpl implements OrderSettingService {
    @Autowired
    OrderSettingDao orderSettingDao;

    //批量添加
    @Override
    public void add(List<OrderSetting> orderSettingList) {
        if (orderSettingList != null && orderSettingList.size() > 0) {
            for (OrderSetting orderSetting : orderSettingList) {
                //检查此数据（日期）是否存在
                Long count = orderSettingDao.findCountByOrderDate(orderSetting.getOrderDate());
                if(count>0){   //已经存在，执行更新操作
                    // 根据预约设置时间(orderDate字段)，更新最多预约人数(number)
                    orderSettingDao.updateNumberByOrderDate(orderSetting);
                }else {
                    //不存在，执行添加操作
                    orderSettingDao.add(orderSetting);
                }
            }
        }
    }

    @Override
    public List<Map> getOrderSettingByMonth(String date) {  //参数格式为：2019-03
        // 1.组织查询Map，dateBegin表示月份开始时间，dateEnd月份结束时间
        String dateBegin = date + "-1";  //2020-09-1
        String dateEnd = date + "-31";   //2020-09-31
        Map map = new HashMap();
        map.put("dateBegin",dateBegin);
        map.put("dateEnd",dateEnd);
        // 2.(根据月份)查询当前月份的预约设置
        List<OrderSetting> list = orderSettingDao.getOrderSettingByMonth(map);
        List<Map> data = new ArrayList();
        // 3.将List<OrderSetting>，组织成List<Map>
        for (OrderSetting orderSetting : list) {
            Map orderSettingMap = new HashMap();
            orderSettingMap.put("date",orderSetting.getOrderDate().getDate());  //获得日期（1-31号）
            orderSettingMap.put("number",orderSetting.getNumber());  //可预约人数
            orderSettingMap.put("reservations",orderSetting.getReservations()); //已预约人数
            data.add(orderSettingMap);
        }
        return data;
    }

    @Override
    public void updateNumberByOrderDate(OrderSetting orderSetting) {
        Long count = orderSettingDao.findCountByOrderDate(orderSetting.getOrderDate());
        if(count>0){
            //当前日期已经进行了预约设置，需要进行更新操作
            orderSettingDao.updateNumberByOrderDate(orderSetting);
        }else {
            //当前日期没有进行预约设置，进行添加操作
            orderSettingDao.add(orderSetting);
        }
    }
}
