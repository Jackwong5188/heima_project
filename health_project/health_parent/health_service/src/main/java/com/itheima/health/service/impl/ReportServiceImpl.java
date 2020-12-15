package com.itheima.health.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.health.dao.MemberDao;
import com.itheima.health.dao.OrderDao;
import com.itheima.health.service.ReportService;
import com.itheima.health.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {
    @Autowired
    private MemberDao memberDao;

    @Autowired
    private OrderDao orderDao;

    /**
     * 获得运营统计数据
     * Map数据格式：
     *      todayNewMember（今日新增会员数） -> number
     *      totalMember（总会员数） -> number
     *      thisWeekNewMember（本周新增会员数） -> number
     *      thisMonthNewMember（本月新增会员数） -> number
     *      todayOrderNumber（今日预约数） -> number
     *      todayVisitsNumber（今日到诊数） -> number
     *      thisWeekOrderNumber（本周预约数） -> number
     *      thisWeekVisitsNumber（本周到诊数） -> number
     *      thisMonthOrderNumber（本月预约数） -> number
     *      thisMonthVisitsNumber（本月到诊数） -> number
     *      hotSetmeal（热门套餐（取前4）） -> List<Map<String,Object>>
     */
    @Override
    public Map<String, Object> getBusinessReportData() throws Exception {
        // 获得当前日期
        String today = DateUtils.parseDate2String(DateUtils.getToday());
        // 获得本周一的日期
        String thisWeekMonday = DateUtils.parseDate2String(DateUtils.getThisWeekMonday());
        // 获取本周最后一天的日期
        String thisWeekSunday = DateUtils.parseDate2String(DateUtils.getSundayOfThisWeek());
        // 获得本月第一天的日期
        String firstDay4ThisMonth = DateUtils.parseDate2String(DateUtils.getFirstDay4ThisMonth());
        // 获得本月第一天的日期
        String lastDay4ThisMonth = DateUtils.parseDate2String(DateUtils.getLastDay4ThisMonth());

        //今日新增会员数(根据日期统计会员数)
        Integer todayNewMember = memberDao.findMemberCountByDate(today);

        //总会员数
        Integer totalMember = memberDao.findMemberTotalCount();

        //本周新增会员数(统计指定日期之后的会员数)
        Integer thisWeekNewMember = memberDao.findMemberCountAfterDate(thisWeekMonday);

        //本月新增会员数(统计指定日期之后的会员数)
        Integer thisMonthNewMember = memberDao.findMemberCountAfterDate(firstDay4ThisMonth);

        //今日预约数(根据日期统计预约数)
        Integer todayOrderNumber = orderDao.findOrderCountByDate(today);

        //今日到诊数(根据日期统计到诊数)
        Integer todayVisitsNumber = orderDao.findVisitsCountByDate(today);

        //本周预约数(统计指定(区间范围)日期的预约数)
        Map<String,Object> weekMap = new HashMap<String,Object>();
        weekMap.put("begin",thisWeekMonday);
        weekMap.put("end",thisWeekSunday);
        Integer thisWeekOrderNumber = orderDao.findOrderCountBetweenDate(weekMap);

        //本周到诊数(统计指定(区间范围)日期的到诊数)
        Map<String,Object> weekMap2 = new HashMap<String,Object>();
        weekMap2.put("begin",thisWeekMonday);
        weekMap2.put("end",thisWeekSunday);
        Integer thisWeekVisitsNumber = orderDao.findVisitsCountBetweenDate(weekMap2);

        //本月预约数
        Map<String,Object> monthMap = new HashMap<String,Object>();
        monthMap.put("begin",firstDay4ThisMonth);
        monthMap.put("end",lastDay4ThisMonth);
        Integer thisMonthOrderNumber = orderDao.findOrderCountBetweenDate(monthMap);

        //本月到诊数
        Map<String,Object> monthMap2 = new HashMap<String,Object>();
        monthMap2.put("begin",firstDay4ThisMonth);
        monthMap2.put("end",lastDay4ThisMonth);
        Integer thisMonthVisitsNumber = orderDao.findVisitsCountBetweenDate(monthMap2);

        //热门套餐（取前4）
        List<Map> hotSetmeal = orderDao.findHotSetmeal();

        Map<String,Object> result = new HashMap<>();
        result.put("reportDate",today);
        result.put("todayNewMember",todayNewMember);
        result.put("totalMember",totalMember);
        result.put("thisMonthNewMember",thisMonthNewMember);
        result.put("thisWeekNewMember",thisWeekNewMember);
        result.put("todayOrderNumber",todayOrderNumber);
        result.put("todayVisitsNumber",todayVisitsNumber);
        result.put("thisWeekVisitsNumber",thisWeekVisitsNumber);
        result.put("thisWeekOrderNumber",thisWeekOrderNumber);
        result.put("thisMonthOrderNumber",thisMonthOrderNumber);
        result.put("thisMonthVisitsNumber",thisMonthVisitsNumber);
        result.put("hotSetmeal",hotSetmeal);

        return result;
    }
}
