package com.lz.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lz.common.enums.UserRole;

import java.util.Date;
import java.util.Map;

/**
 * JWT Utility
 */
public class JwtUtil {

    public static String genToken(Map<String, Object> claims, String key) {
        return genToken(claims, key, 1000 * 60 * 60 * 24 * 7L);
    }

    public static String genToken(Map<String, Object> claims, String key, long expireMillis) {
        Object o = claims.get("role");
        if (o == null) {
            claims.put("role", "user");
        }
        if (o instanceof UserRole){
            claims.put("role", ((UserRole) o).getRole());
        }
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + expireMillis))
                .sign(Algorithm.HMAC256(key));
    }

    public static Map<String, Object> parseToken(String token, String key) {
        return JWT.require(Algorithm.HMAC256(key))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

    public static boolean isExpired(String token, String key) {
        try {
            Date expiresAt = JWT.require(Algorithm.HMAC256(key))
                    .build()
                    .verify(token)
                    .getExpiresAt();
            return expiresAt.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
