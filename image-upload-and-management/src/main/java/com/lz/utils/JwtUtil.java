package com.lz.utils;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: lz
 * @Date: 2023/11/08/10:14
 * @Description:
 */

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Date;
import java.util.Map;

/**
 * @author lz
 */
public class JwtUtil {
    
    /**
     * 生成JWT
     *
     * @param claims 索赔
     *
     * @return {@code String}
     */
    public static String genToken(Map<String, Object> claims,String KEY){
       
        return JWT.create()
                .withClaim("claims",claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000* 60 * 60 * 12))
                .sign(Algorithm.HMAC256(KEY));
    }

    /**
     * 解析令牌
     *
     * @param token 令 牌
     *
     * @return {@code Map<String,Object>}
     */
    public static Map<String,Object> parseToken(String token,String KEY){
        return JWT.require(Algorithm.HMAC256(KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}