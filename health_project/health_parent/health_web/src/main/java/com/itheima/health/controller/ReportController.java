package com.itheima.health.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.entity.Result;
import com.itheima.health.service.MemberService;
import com.itheima.health.service.ReportService;
import com.itheima.health.service.SetmealService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 统计报表
 */
@RestController
@RequestMapping("/report")
public class ReportController {
    @Reference  // 订阅 dubbo注解
    private MemberService memberService;

    @Reference
    private SetmealService setmealService;

    @Reference
    private ReportService reportService;

    // 统计报表（会员数量折线图统计）
    @RequestMapping(value = "/getMemberReport")
    public Result getMemberReport(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-12);  // 获取过去12个月，2019-9

        List<String> months = new ArrayList<String>();
        for(int i=0;i<12;i++){
            // 过去的12个月，输出
            calendar.add(Calendar.MONTH,1);  //2019-10
            Date date = calendar.getTime();
            String sDate = new SimpleDateFormat("yyyy-MM").format(date);
            months.add(sDate);
        }
        Map<String, Object> map = new HashMap<>();
        //list中元素：[2019-10, 2019-11, 2019-12, 2020-01, 2020-02, 2020-03, 2020-04, 2020-05, 2020-06, 2020-07, 2020-08, 2020-09]
        map.put("months",months);

        //根据月份查找会员数量
        List<Integer> memberCount = memberService.findMemberCountByMonth(months);
        map.put("memberCount",memberCount);

        return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS,map);
    }

    /**
     * 预约套餐占比统计
     * @return
     */
    @RequestMapping("/getSetmealReport")
    public Result getSetmealReport(){
       /* {
            "data":{
                    "setmealNames":["套餐1","套餐2","套餐3"],
                    "setmealCount":[
                                    {"name":"套餐1","value":10},
                                    {"name":"套餐2","value":30},
                                    {"name":"套餐3","value":25}
                                   ]
                   },
            "flag":true,
            "message":"获取套餐统计数据成功" }     */

        //查找套餐数量
        List<Map<String, Object>> list = setmealService.findSetmealCount();

        Map<String,Object> map = new HashMap<String,Object>();
        //封装套餐数量
        map.put("setmealCount",list);

        //封装套餐名字
        List<String> setmealNames = new ArrayList<String>();
        for (Map<String, Object> m : list) {
            String name = (String) m.get("name");
            setmealNames.add(name);
        }
        map.put("setmealNames",setmealNames);

        return new Result(true, MessageConstant.GET_SETMEAL_COUNT_REPORT_SUCCESS,map);
    }

    /**
     * 获取运营统计数据
     * @return
     */
    @RequestMapping("/getBusinessReportData")
    public Result getBusinessReportData(){
     /*   {
          "data":{
            "reportDate":"2019-04-25",
            "todayNewMember":0,
              "totalMember":10,
            "thisMonthNewMember":2,
            "thisWeekNewMember":0,
              "todayOrderNumber":0,
              "todayVisitsNumber":0,
            "thisWeekVisitsNumber":0,
              "thisWeekOrderNumber":0,
            "thisMonthOrderNumber":2,
            "thisMonthVisitsNumber":0,
            "hotSetmeal":[
              {"proportion":0.4545,"name":"粉红珍爱(女)升级TM12项筛查体检套餐","setmeal_count":5},
              {"proportion":0.1818,"name":"美丽爸妈升级肿瘤12项筛查体检套餐","setmeal_count":2},
              {"proportion":0.1818,"name":"珍爱高端升级肿瘤12项筛查","setmeal_count":2},
              {"proportion":0.0909,"name":"孕前检查套餐","setmeal_count":1}
            ],
                },
          "flag":true,
          "message":"获取运营统计数据成功"
        }*/

        try {
            Map<String, Object> map = reportService.getBusinessReportData();
            return new Result(true,MessageConstant.GET_BUSINESS_REPORT_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }

    /**
     * 导出Excel报表
     * @return
     */
    @RequestMapping("/exportBusinessReport")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response){
        try {
            //远程调用报表服务获取报表数据
            Map<String, Object> result = reportService.getBusinessReportData();

            //取出返回结果数据，准备将报表数据写入到Excel文件中
            String reportDate = (String) result.get("reportDate");
            Integer todayNewMember = (Integer) result.get("todayNewMember");
            Integer totalMember = (Integer) result.get("totalMember");
            Integer thisWeekNewMember = (Integer) result.get("thisWeekNewMember");
            Integer thisMonthNewMember = (Integer) result.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer) result.get("todayOrderNumber");
            Integer thisWeekOrderNumber = (Integer) result.get("thisWeekOrderNumber");
            Integer thisMonthOrderNumber = (Integer) result.get("thisMonthOrderNumber");
            Integer todayVisitsNumber = (Integer) result.get("todayVisitsNumber");
            Integer thisWeekVisitsNumber = (Integer) result.get("thisWeekVisitsNumber");
            Integer thisMonthVisitsNumber = (Integer) result.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>) result.get("hotSetmeal");

            //获得Excel模板文件绝对路径
            String temlateRealPath = request.getSession().getServletContext().getRealPath("template") + File.separator + "report_template.xlsx";

            //使用POI，读取工作簿，读取工作表，读取行，将数据填充到单元格
            XSSFWorkbook workbook = new XSSFWorkbook(new File(temlateRealPath));
            XSSFSheet sheet = workbook.getSheetAt(0);  // sheet1

            // 日期
            XSSFRow row3 = sheet.getRow(2);  // 从0开始, row3表示：第三行
            row3.getCell(5).setCellValue(reportDate);

            // 会员相关
            XSSFRow row5 = sheet.getRow(4);  // 从0开始, row5表示：第5行
            row5.getCell(5).setCellValue(todayNewMember);  //今日新增会员数
            row5.getCell(7).setCellValue(totalMember);  //总会员数

            XSSFRow row6 = sheet.getRow(5);  // 从0开始, row6表示：第6行
            row6.getCell(5).setCellValue(thisWeekNewMember);  //本周新增会员数
            row6.getCell(7).setCellValue(thisMonthNewMember);  //本月新增会员数

            // 预约订单相关
            XSSFRow row8 = sheet.getRow(7);  // 从0开始, row8表示：第8行
            row8.getCell(5).setCellValue(todayOrderNumber);  //今日预约数
            row8.getCell(7).setCellValue(todayVisitsNumber);  //今日到诊数

            XSSFRow row9 = sheet.getRow(8);  // 从0开始, row9表示：第9行
            row9.getCell(5).setCellValue(thisWeekOrderNumber);  //本周预约数
            row9.getCell(7).setCellValue(thisWeekVisitsNumber);  //本周到诊数

            XSSFRow row10 = sheet.getRow(9);  // 从0开始, row10表示：第10行
            row10.getCell(5).setCellValue(thisMonthOrderNumber);  //本月预约数
            row10.getCell(7).setCellValue(thisMonthVisitsNumber);  //本月到诊数

            //热门套餐
            int rowNum = 12;  //第13行
            for (Map map : hotSetmeal) {
                String name = (String) map.get("name");
                Long setmeal_count = (Long) map.get("setmeal_count");
                BigDecimal proportion = (BigDecimal) map.get("proportion");
                XSSFRow row13 = sheet.getRow(rowNum++);  // ++放置到后面，先第13行，再根据循环累加
                row13.getCell(4).setCellValue(name);  //套餐名称
                row13.getCell(5).setCellValue(setmeal_count);  //预约数量
                row13.getCell(6).setCellValue(String.valueOf(proportion));  //占比
            }

            //将excel文件以IO的形式导出（设置类型和下载方式）
            ServletOutputStream out = response.getOutputStream();
            // 设置类型
            response.setContentType("application/vnd.ms-excel"); // 不指定。默认是以文本的形式输出
            // 设置下载方式（"attachment;filename="+filename：表示附件的方式下载；默认inline：表示内连，在浏览器上直接查看）
            String filename = "businessReport85.xlsx";
            response.setHeader("Content-Disposition","attachment;filename="+filename);
            workbook.write(out);
            // 刷新和关闭
            out.flush();
            out.close();
            workbook.close();

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }

    /**
     * 导出PDF报表
     * @return
     */
    @RequestMapping("/exportBusinessReport4PDF")
    public Result exportBusinessReport4PDF(HttpServletRequest request, HttpServletResponse response){
        try {
            // 1：读取放置到pdf中的内容数据
            Map<String, Object> map = reportService.getBusinessReportData();
            // 热门套餐
            List<Map> hotSetmeal = (List<Map>) map.get("hotSetmeal");

            // 2：获取模板文件绝对路径
            String jrxmlPath = request.getSession().getServletContext().getRealPath("template") + File.separator + "health_business3.jrxml";
            String jasperPath = request.getSession().getServletContext().getRealPath("template") + File.separator + "health_business3.jasper";

            // 3：将PDF文件以IO的形式导出（设置类型和下载方式）
            ServletOutputStream out = response.getOutputStream();
            // 设置类型
            response.setContentType("application/pdf"); // 不指定。默认是以文本的形式输出
            // 设置下载方式（"attachment;filename="+filename：表示附件的方式下载；默认inline：表示内连，在浏览器上直接查看）
            String filename = "businessReportPDF85.pdf";
            response.setHeader("Content-Disposition","attachment;filename="+filename);

            // 4：使用JasperReport的API导出PDF
            // (1)：编译模板
            JasperCompileManager.compileReportToFile(jrxmlPath,jasperPath);
            // (2)：填充数据---使用JavaBean数据源方式填充
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, map, new JRBeanCollectionDataSource(hotSetmeal));
            // (3)：输出PDF（流）
            JasperExportManager.exportReportToPdfStream(jasperPrint,out);

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
