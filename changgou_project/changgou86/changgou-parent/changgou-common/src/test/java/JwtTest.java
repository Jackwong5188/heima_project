import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    /****
     * 创建Jwt令牌
     */
    @Test
    public void testCreateJwt(){
        JwtBuilder builder= Jwts.builder()
                .setId("888")             //设置唯一编号
                .setSubject("小白")       //设置主题  可以是JSON数据
                .setIssuedAt(new Date())  //设置签发日期
                //.setExpiration(new Date()) //设置过期时间 ，参数为Date类型数据
                .signWith(SignatureAlgorithm.HS256,"itcast");//设置签名 使用HS256算法，并设置SecretKey(字符串)

        //自定义数据
        Map<String, Object> userinfo = new HashMap<>();
        userinfo.put("name","王俊凯");
        userinfo.put("age",19);
        userinfo.put("school","北京电影学院");
        builder.addClaims(userinfo);

        //构建 并返回一个字符串
        System.out.println( builder.compact() );
    }

    /***
     * 解析Jwt令牌数据
     */
    @Test
    public void testParseJwt(){
        String compactJwt="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE2MDU4Mzg0NjgsInNjaG9vbCI6IuWMl-S6rOeUteW9seWtpumZoiIsIm5hbWUiOiLnjovkv4rlh68iLCJhZ2UiOiIxOSJ9.5cWdFfy06os-_PMAMls1AeZt2NGHvHgpFgManXDjiTE";
        Claims claims = Jwts.parser().
                setSigningKey("itcast").
                parseClaimsJws(compactJwt).
                getBody();
        System.out.println(claims);
    }
}