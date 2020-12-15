package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${weixin.appid}")
    private String appid;         //微信公众账号或开放平台APP的唯一标识

    @Value("${weixin.partner}")
    private String partner;       //财付通平台的商户账号

    @Value("${weixin.partnerkey}")
    private String partnerkey;   //财付通平台的商户密钥

    @Value("${weixin.notifyurl}")
    private String notifyurl;    //回调地址

    /****
     * 创建二维码
     * @param username : 用户名
     * @param out_trade_no : 订单编号
     * @param total_fee    : 交易金额,单位：分
     * @param from :  1：标识普通队列;  2：标识秒杀队列
     * @return
     */
    @Override
    public Map<String, String> createNative(Map<String,String> parameter) {
        try {
            //1、封装参数
            Map paramMap = new HashMap();
            paramMap.put("appid", appid);                              //应用ID
            paramMap.put("mch_id", partner);                           //商户ID号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());   //随机数
            paramMap.put("body", "畅购");                            	//订单描述
            paramMap.put("out_trade_no",parameter.get("out_trade_no"));                 //商户订单号
            paramMap.put("total_fee", parameter.get("total_fee"));                      //交易金额
            paramMap.put("spbill_create_ip", "127.0.0.1");           //终端IP
            paramMap.put("notify_url", notifyurl);                    //回调地址
            paramMap.put("trade_type", "NATIVE");                     //交易类型
            //添加附加数据, 要用from、username了
            paramMap.put("attach", JSON.toJSONString(parameter));

            //2、将paramMap转成xml，并携带签名(商户密钥)
            String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            //3、执行请求(参考讲义中1.4HttpClient工具类使用的步骤)
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);//是否是https协议
            client.setXmlParam(paramXml);//发送的xml数据
            client.post(); //执行post请求

            //4、获取参数
            String content = client.getContent(); //获取结果
            //xml再转换成MAP
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);
            System.out.println("============创建二维码============");
            System.out.println("stringMap:"+stringMap);

            //5、获取部分页面所需参数
            Map<String,String> dataMap = new HashMap<String,String>();
            dataMap.put("code_url",stringMap.get("code_url"));  //二维码url
            dataMap.put("out_trade_no",parameter.get("out_trade_no"));  //订单编号
            dataMap.put("total_fee",parameter.get("total_fee"));         //交易金额

            //6.返回
            return dataMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 查询支付状态
     * @param out_trade_no : 订单编号
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {
        try {
            //1.封装参数
            //1.封装参数
            Map param = new HashMap();
            param.put("appid",appid);                            //应用ID
            param.put("mch_id",partner);                         //商户号
            param.put("out_trade_no",out_trade_no);              //商户订单编号
            param.put("nonce_str",WXPayUtil.generateNonceStr()); //随机字符

            //2、将参数转成xml字符，并携带签名
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);

            //3、发送请求
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);//是否是https协议
            client.setXmlParam(paramXml);//发送的xml数据
            client.post(); //执行post请求

            //4、获取返回值，并将返回值转成Map
            String content = client.getContent(); //获取结果
            //xml再转换成MAP
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);
            return stringMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
