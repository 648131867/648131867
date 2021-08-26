package com.tanhua.server.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    @Test
    public void testJwt(){
        // 签名是使用的盐，密钥
        String secret = "itcast";

        // 私有信息, 非敏感数据
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("mobile", "12345789");
        claims.put("id", "2");

        // 生成token
        String jwt = Jwts.builder()
            .setClaims(claims) //设置响应数据体
            .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密盐
            .compact();

        System.out.println(jwt); //eyJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMjM0NTc4OSIsImlkIjoiMiJ9.VivsfLzrsKFOJo_BdGIf6cKY_7wr2jMOMOIGaFt_tps

        // 通过token解析数据
        Map<String, Object> body = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(jwt)
            .getBody();

        System.out.println(body); //{mobile=12345789, id=2}
    }
}
