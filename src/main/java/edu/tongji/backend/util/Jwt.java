package edu.tongji.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class Jwt {
    private static String _key = PropertiesHelper.GetKey("JWT_KEY");
    private static final String key = _key.isEmpty()?"Tangxiaozhi2023":_key;
    // 生成token
    public static String generate(Map<String, Object> jwtInfo){
        // 7天
        long expire = 7L * 24 * 60 * 60 * 1000;
        return Jwts.builder()
                .addClaims(jwtInfo)
                .signWith(SignatureAlgorithm.HS256, key)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
    }

    // 解析token
    public static Claims parse(String jwt){
        //去掉jwt的前7个字符
       jwt = jwt.substring(7);
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt)
                .getBody();
    }
}
