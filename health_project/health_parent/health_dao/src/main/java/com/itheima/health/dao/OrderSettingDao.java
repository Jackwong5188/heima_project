package com.itheima.health.dao;

import com.itheima.health.pojo.OrderSetting;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OrderSettingDao {
    Long findCountByOrderDate(Date orderDate);

    void updateNumberByOrderDate(OrderSetting orderSetting);

    void add(OrderSetting orderSetting);

    List<OrderSetting> getOrderSettingByMonth(Map map);

    OrderSetting findOrderSettingByOrderDate(Date date);

    void updateReservationsByOrderDate(Date date);
}
