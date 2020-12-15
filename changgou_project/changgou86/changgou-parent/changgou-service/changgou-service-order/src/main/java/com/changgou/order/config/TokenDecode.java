package com.changgou.order.config;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenDecode {
    private static final String PUBLIC_KEY = "public.key";

    private String getPubKey() {
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * 获取用户信息
     * @return
     */
    public Map<String, String> getUserInfo() {
        //1.根据ID 调用goods的feign获取商品的数据
        //2.将数据存储到redis的 某一个登录的用户的 购物车中 key: value
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        //1.先获取到令牌的信息
        String tokenValue = details.getTokenValue();
        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(tokenValue, new RsaVerifier(getPubKey()));
        //获取Jwt原始内容
        //2.解析令牌信息 获取到username的值 当前的登录的用户的用户名
        String claims = jwt.getClaims();

        Map<String, String> map = JSON.parseObject(claims, Map.class);

        return map;
    }
    //获取用户名

    public String getUsername() {
        return getUserInfo().get("user_name");
    }
}
