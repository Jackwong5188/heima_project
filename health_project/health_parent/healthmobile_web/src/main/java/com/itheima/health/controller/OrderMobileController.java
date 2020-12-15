package com.itheima.health.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.aliyuncs.exceptions.ClientException;
import com.itheima.health.constant.MessageConstant;
import com.itheima.health.constant.RedisMessageConstant;
import com.itheima.health.entity.Result;
import com.itheima.health.pojo.CheckGroup;
import com.itheima.health.pojo.CheckItem;
import com.itheima.health.pojo.Order;
import com.itheima.health.pojo.Setmeal;
import com.itheima.health.service.OrderService;
import com.itheima.health.service.SetmealService;
import com.itheima.health.utils.SMSUtils;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * 体检预约
 */
@RestController
@RequestMapping("/order")
public class OrderMobileController {
    @Reference
    OrderService orderService;

    // Redis本机（单机）
    //@Autowired
    //JedisPool jedisPool;

    // Redis集群
    @Autowired
    RedisTemplate redisTemplate;

    @Reference
    SetmealService setmealService;

    //(使用订单id)在预约成功页面，导出PDF文档
    @RequestMapping(value = "/exportSetmealInfo")
    public Result exportSetmealInfo(Integer id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        /**第一步：查询预约信息，包括体检人信息、套餐信息、体检日期、预约类型、套餐id*/
        Map map = orderService.findById4Detail(id);
        // 获取套餐ID
        Integer setmealId = (Integer) map.get("setmealId");
        // 使用套餐ID，查询套餐信息
        Setmeal setmeal = setmealService.findById(setmealId);

        /**第二步：生成PDF报表*/
        // 1：创建一个文档对象
        Document document = new Document();
        // 2：获取1个PdfWriter对象实例
        PdfWriter.getInstance(document,response.getOutputStream());
        // 设置导出类型(指定PDF类型)
        response.setContentType("application/pdf"); // 不指定。默认是以文本的形式输出
        // 设置下载方式（"attachment;filename="+filename：表示附件的方式下载；默认inline：表示内连，在浏览器上直接查看）
        String filename = "mobileSetmealOrder85.pdf";
        response.setHeader("Content-Disposition","attachment;filename="+filename);
        // 解决中文问题
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font font = new Font(bfChinese,12, Font.NORMAL, Color.BLUE);
        // 3：打开文档（方便添加数据）
        document.open();
        // 4：添加数据
        document.add(new Paragraph("体检人："+map.get("member"),font));
        document.add(new Paragraph("体检套餐："+map.get("setmeal"),font));
        document.add(new Paragraph("体检日期："+map.get("orderDate"),font));
        document.add(new Paragraph("预约类型："+map.get("orderType"),font));

        //创建3列的表格
        Table table = new Table(3);

        //设置表格的形式  
        table.setWidth(80); // 宽度
        table.setBorder(1); // 边框
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER); //水平对齐方式
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER); // 垂直对齐方式
        /*设置表格属性*/
        table.setBorderColor(new Color(0, 0, 255)); //将边框的颜色设置为蓝色
        table.setPadding(5);//设置表格与字体间的间距
        //table.setSpacing(5);//设置表格上下的间距
        table.setAlignment(Element.ALIGN_CENTER);//设置字体显示居中样式

        //设置表头的名称
        table.addCell(buildCell("项目名称",font));
        table.addCell(buildCell("项目内容",font));
        table.addCell(buildCell("项目解读",font));

        //写数据
        for (CheckGroup checkGroup : setmeal.getCheckGroups()) {
            // 检查组的名称
            table.addCell(buildCell(checkGroup.getName(),font));
            // 组织检查项集合
            StringBuffer checkItems = new StringBuffer();
            for (CheckItem checkItem : checkGroup.getCheckItems()) {
                checkItems.append(checkItem.getName()+"  ");
            }
            // 检查项(的名称)集合
            table.addCell(buildCell(checkItems.toString(),font));
            // 检查组的备注
            table.addCell(buildCell(checkGroup.getRemark(),font));
        }
        // 将表格加入文档
        document.add(table);

        // 5：关闭文档
        document.close();
        return null;
    }

    // 传递内容和字体样式，生成单元格
    private Cell buildCell(String content, Font font) throws BadElementException {
        return new Cell(new Phrase(content,font));
    }

    /**
     * 体检预约
     * @param map
     * @return
     */
    @RequestMapping("/submit")
    public Result submitOrder(@RequestBody Map map){
        //获取用户输入的手机号
        String telephone = (String) map.get("telephone");
        //获取用户输入的验证码
        String validateCode = (String) map.get("validateCode");
        //从Redis中获取缓存的验证码，key为手机号+RedisConstant.SENDTYPE_ORDER
        //String codeInRedis = jedisPool.getResource().get(telephone + RedisMessageConstant.SENDTYPE_ORDER);
        String codeInRedis = (String) redisTemplate.opsForValue().get(telephone + RedisMessageConstant.SENDTYPE_ORDER);
        //校验手机验证码
        if(codeInRedis == null || !codeInRedis.equals(validateCode)){ // Redis中获取缓存的验证码 与 用户输入的验证码 不相等
            return new Result(false, MessageConstant.VALIDATECODE_ERROR);
        }
        Result result =null;
        //调用体检预约服务
        try {
            map.put("orderType", Order.ORDERTYPE_WEIXIN);
            result = orderService.order(map);
        } catch (Exception e) {
            e.printStackTrace();
            //预约失败
            return result;
        }
        if(result.isFlag()){
            //预约成功，发送短信通知，短信通知内容可以是“预约时间”，“预约人”，“预约地点”，“预约事项”等信息。
            String orderDate = (String) map.get("orderDate");
            try {
                SMSUtils.sendShortMessage(telephone,orderDate);
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 使用订单id，查询订单的详情信息
    @RequestMapping(value = "/findById")
    public Result findById(Integer id){
        Map map = null;
        try {
            //根据订单id查询预约信息，包括体检人信息、套餐信息、体检日期、预约类型
            map = orderService.findById4Detail(id);
            //查询预约信息成功
            return new Result(true,MessageConstant.QUERY_ORDER_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            //查询预约信息失败
            return new Result(false,MessageConstant.QUERY_ORDER_FAIL);
        }
    }
}
