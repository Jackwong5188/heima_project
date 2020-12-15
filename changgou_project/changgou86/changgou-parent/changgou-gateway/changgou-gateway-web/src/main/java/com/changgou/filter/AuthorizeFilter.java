package com.changgou.filter;

import com.changgou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import net.minidev.json.JSONUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//自定义全局过滤器
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";
    //登录页面
    private static final String LOGIN_URL = "http://localhost:9001/oauth/login";

    /***
     * 全局过滤器
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取Request、Response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取请求的URI
        String path = request.getURI().getPath();

        //如果是 登录请求(用户微服务里面),则直接放行
        if (path.startsWith("/api/user/login")) {
            //放行
            Mono<Void> filter = chain.filter(exchange);
            return filter;
        }

        //获取头文件中的令牌信息
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);

        //如果头文件中没有，则从请求参数中获取
        if(StringUtils.isEmpty(token)){
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }

        //从Cookie中获取令牌数据
        HttpCookie first = request.getCookies().getFirst(AUTHORIZE_TOKEN);
        if(first!=null){
            token = first.getValue();
        }

        //如果为空，则输出错误代码
        if(StringUtils.isEmpty(token)){
            //设置方法不允许被访问，401错误代码
            //response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //说明用户没登录，则跳转到登录页面。 拼接 用户原本请求的URI
            response.getHeaders().set("Location",LOGIN_URL+"?FROM="+request.getURI().toString());
            response.setStatusCode(HttpStatus.SEE_OTHER);  // 状态码：303
            return response.setComplete();
        }

        //解析令牌数据
        try {
            //Claims claims = JwtUtil.parseJWT(token);
            //将令牌数据添加到头文件
            //request.mutate().header(AUTHORIZE_TOKEN,claims.toString());
            request.mutate().header(AUTHORIZE_TOKEN,"bearer "+token);
        } catch (Exception e) {
            e.printStackTrace();
            //解析失败，响应401错误
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //放行
        return chain.filter(exchange);
    }

    /***
     * 过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
