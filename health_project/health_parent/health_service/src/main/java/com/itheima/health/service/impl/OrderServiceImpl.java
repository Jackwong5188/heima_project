package com.itheima.health.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.dao.MemberDao;
import com.itheima.health.dao.OrderDao;
import com.itheima.health.dao.OrderSettingDao;
import com.itheima.health.entity.Result;
import com.itheima.health.pojo.Member;
import com.itheima.health.pojo.Order;
import com.itheima.health.pojo.OrderSetting;
import com.itheima.health.service.OrderService;
import com.itheima.health.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 体检预约服务
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderSettingDao orderSettingDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private OrderDao orderDao;

    //体检预约
    @Override
    public Result order(Map map) throws Exception {
        //检查当前日期是否进行了预约设置
        String orderDate = (String) map.get("orderDate");
        Date date = DateUtils.parseString2Date(orderDate);
        //根据预约日期查找预约设置对象
        OrderSetting orderSetting = orderSettingDao.findOrderSettingByOrderDate(date);
        if(orderSetting == null){  //所选日期不能进行体检预约
            return new Result(false, MessageConstant.SELECTED_DATE_CANNOT_ORDER);
        }

        //检查预约日期是否预约已满
        int number = orderSetting.getNumber();//可预约人数
        int reservations = orderSetting.getReservations();//已预约人数
        if(reservations >= number){
            //预约已满，不能预约
            return new Result(false, MessageConstant.ORDER_FULL);
        }

        //检查当前用户是否为会员，根据手机号判断
        String telephone = (String) map.get("telephone");
        //根据手机号查找会员
        Member member = memberDao.findMemberByTelephone(telephone);
        //如果是会员，防止重复预约（一个会员、一个时间、一个套餐不能重复，否则是重复预约）
        if (member != null) {
            Integer memberId = member.getId();
            int setmealId = Integer.parseInt((String) map.get("setmealId"));
            Order order = new Order(memberId, date, null, null, setmealId);
            List<Order> list = orderDao.findOrderListByCondition(order);  //根据订单查询订单列表
            if (list != null && list.size() > 0) {
                //已经完成了预约，不能重复预约
                return new Result(false, MessageConstant.HAS_ORDERED);
            }
        }

        //当前用户不是会员，需要添加到会员表
        if (member == null) {
            member = new Member();
            member.setName((String) map.get("name"));
            member.setPhoneNumber((String) map.get("telephone"));
            member.setIdCard((String) map.get("idCard")); //身份证号
            member.setSex((String) map.get("sex"));
            member.setRegTime(new Date());
            //新增会员
            memberDao.add(member);
        }

        //可以预约，设置预约人数加一
        orderSettingDao.updateReservationsByOrderDate(date);

        //保存预约信息到预约表
        Order order = new Order(member.getId(),  //会员id
                date,       //预约日期
                (String) map.get("orderType"),  //预约类型
                Order.ORDERSTATUS_NO,   //未到诊
                Integer.parseInt((String) map.get("setmealId")));  //套餐id

        orderDao.add(order);
        return new Result(true, MessageConstant.ORDER_SUCCESS, order);
    }

    //根据订单id查询预约信息，包括体检人信息、套餐信息、体检日期、预约类型、套餐id
    @Override
    public Map findById4Detail(Integer id) throws Exception {
        Map map = orderDao.findById4Detail(id);
        if(map != null){
            //处理日期格式
            Date orderDate = (Date) map.get("orderDate");
            map.put("orderDate",DateUtils.parseDate2String(orderDate));
        }
        return map;
    }
}
