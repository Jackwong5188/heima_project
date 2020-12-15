/**
 * Copyright (c) 2020 itheima.com, All rights reserved.
 *
 * @Author: lvyang
 */
package com.tensquare.encrypt.filter;

import com.google.common.base.Charsets;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.http.ServletInputStreamWrapper;
import com.tensquare.encrypt.rsa.RsaKeys;
import com.tensquare.encrypt.service.RsaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * @Description:
 * @Author: lvyang
 * @Created Date: 2020年04月20日
 * @LastModifyDate:
 * @LastModifyBy:
 * @Version:
 */
@Component
public class DecryptFilter extends ZuulFilter {

    @Autowired
    RsaService rsaService;

    @Override
    public String filterType() {
        return "pre";//路由前过滤
    }

    @Override
    public int filterOrder() {
        return 0;//数值越小越先过滤
    }

    @Override
    public boolean shouldFilter() {
        return true;//返回ture就是要过滤
    }

    /*
     * 将加密的参数进行解密，传输给后台的微服务
     * */
    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        //1.获取request
        HttpServletRequest request = currentContext.getRequest();
        //2.获取加密参数
        try {
            ServletInputStream inputStream = request.getInputStream();
            String encryptParams = StreamUtils.copyToString(inputStream, Charsets.UTF_8);
            if(StringUtils.isEmpty(encryptParams)){
                return null;
            }
            //3.解密
            String decryptParams = rsaService.rsaDecryptDataPEM(encryptParams, RsaKeys.getServerPrvKeyPkcs8());
            //4.将解密的参数替换到原本的request中
            byte[] bytes = decryptParams.getBytes();
            currentContext.setRequest(new HttpServletRequestWrapper(request){
                @Override
                public int getContentLength() {
                    return bytes.length;
                }

                @Override
                public long getContentLengthLong() {
                    return bytes.length;
                }

                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new ServletInputStreamWrapper(bytes);
                }
            });
            //5.通知后台微服务参数是json格式，加请求头就行
            currentContext.addZuulRequestHeader("Content-Type", "application/json;charset=UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}