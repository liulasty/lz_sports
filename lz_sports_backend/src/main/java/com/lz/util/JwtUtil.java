package com.lz.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

/**
 * JWT Utility
 */
public class JwtUtil {

    public static String genToken(Map<String, Object> claims, String key) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 12)) // 12 hours
                .sign(Algorithm.HMAC256(key));
    }

    public static Map<String, Object> parseToken(String token, String key) {
        return JWT.require(Algorithm.HMAC256(key))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
}
