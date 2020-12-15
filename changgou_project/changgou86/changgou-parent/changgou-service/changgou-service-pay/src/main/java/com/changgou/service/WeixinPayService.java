package com.changgou.service;

import java.util.Map;

public interface WeixinPayService {
    /*****
     * 创建二维码
     * @param out_trade_no : 订单编号
     * @param total_fee    : 交易金额,单位：分
     * @return
     */
    Map<String, String> createNative(Map<String,String> parameter);

    /***
     * 查询支付状态
     * @param out_trade_no : 订单编号
     * @return
     */
    Map<String, String> queryPayStatus(String out_trade_no);
}
