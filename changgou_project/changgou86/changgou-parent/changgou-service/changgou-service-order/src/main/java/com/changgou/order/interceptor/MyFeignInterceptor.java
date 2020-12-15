package com.changgou.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Component
public class MyFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        //方法就是当调用feign的时候会自动的执行
        //1.获取原来请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            //2.获取源请求对象中的头信息
            //3.获取到所有的头信息，包括令牌的头信息的值 一起全都传递过去
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                //头文件的key
                String headerName = headerNames.nextElement();
                //头文件的value
                String headerValue = request.getHeader(headerName);
                //4.将头信息(令牌数据)传递到下游的微服务中（调用feign的时候传递头过去）
                template.header(headerName,headerValue);
            }
        }
    }
}
