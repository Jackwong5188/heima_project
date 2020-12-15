package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/weixin/pay")
@CrossOrigin
public class WeixinPayController {
    @Value("${mq.pay.exchange.order}")
    private String exchange;

    @Value("${mq.pay.queue.order}")
    private String queue;

    @Value("${mq.pay.routing.key}")
    private String routing;

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment environment;

    /*****
     * 创建二维码
     * @param username : 用户名
     * @param out_trade_no : 订单编号
     * @param total_fee    : 交易金额,单位：分
     * @param from :  1：标识普通队列;  2：标识秒杀队列
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String,String> parameter){
        Map<String,String> resultMap = weixinPayService.createNative(parameter);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }

    /***
     * 支付状态查询
     * @param out_trade_no : 订单编号
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String out_trade_no){
        Map<String,String> resultMap = weixinPayService.queryPayStatus(out_trade_no);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }

    /***
     * 支付回调
     * @param request
     * @return
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request){
        ServletInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            //1.接收微信通知的数据
            inputStream = request.getInputStream();
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,len);
            }

            // 将支付回调数据转换成xml字符串
            String result = new String(outputStream.toByteArray(),"utf-8");
            //将xml字符串转换成Map结构
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println("============支付回调，WeixinPayController类中notifyUrl方法，发送消息给RabbitMQ============");
            System.out.println(map);

            //2.发送消息到MQ  获取from的值
            String attach = map.get("attach"); //获取附加(JSON)数据
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
            String from = attachMap.get("from"); //获取标识，1：普通队列  2: 秒杀队列
            switch (from){
                case "1":
                    System.out.println("发送消息到普通队列");
                    //发送消息给RabbitMQ
                    rabbitTemplate.convertAndSend(
                            environment.getProperty("mq.pay.exchange.order"),
                            environment.getProperty("mq.pay.routing.key"),
                            JSON.toJSONString(map)
                    );
                    break;
                case "2":
                    System.out.println("发送消息到秒杀队列");
                    rabbitTemplate.convertAndSend(
                            environment.getProperty("mq.pay.exchange.seckillorder"),
                            environment.getProperty("mq.pay.routing.seckillkey"),
                            JSON.toJSONString(map)
                    );
                    break;
                default:
                    System.out.println("错误的信息");
                    break;
            }

            //3.返回数据给微信
            Map respMap = new HashMap();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            //MAP再转换成xml
            String xmlString = WXPayUtil.mapToXml(respMap);
            return xmlString;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
