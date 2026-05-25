package com.example.campus.utils;

import com.example.campus.constant.CacheConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private static final String SECRET = "CampusEvaluationSecretKey12345678";

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION = 1000 * 60 * 60 * 24;

    private static RedisUtil redisUtil;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        JwtUtil.redisUtil = redisUtil;
    }

    public static String generateToken(String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    public static String generateToken(String userId) {
        return generateToken(userId, "USER");
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    public static String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public static void blacklistToken(String token) {
        try {
            Claims claims = parseToken(token);
            long expiration = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            long ttl = expiration - now;

            if (ttl > 0) {
                String blacklistKey = String.format(CacheConstant.TOKEN_BLACKLIST, token);
                redisUtil.set(blacklistKey, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isTokenBlacklisted(String token) {
        String blacklistKey = String.format(CacheConstant.TOKEN_BLACKLIST, token);
        return redisUtil.hasKey(blacklistKey);
    }
}
