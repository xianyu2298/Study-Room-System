package edu.jjxy.studyroom.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-time}")
    private long expireTime;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Value("${jwt.header-name}")
    private String headerName;

    /**
     * 生成Token
     */
    public String generateToken(Long userId, Integer role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireTime);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT已过期 - token: {}", token);
            throw e;
        } catch (Exception e) {
            log.warn("JWT解析失败 - error: {}", e.getMessage());
            throw new RuntimeException("JWT解析失败", e);
        }
    }

    /**
     * 获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 获取用户角色
     */
    public Integer getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", Integer.class);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpiration(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 判断Token是否即将过期（剩余时间小于5分钟）
     */
    public boolean isTokenExpiringSoon(String token) {
        Date expiration = getExpiration(token);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        return remainingTime < 5 * 60 * 1000;
    }

    /**
     * 获取Token前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * 获取Header名称
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * 完整Token（包含前缀）
     */
    public String getFullToken(String token) {
        return tokenPrefix + " " + token;
    }

    /**
     * 从完整Token中提取原始Token
     */
    public String extractToken(String fullToken) {
        if (fullToken != null && fullToken.startsWith(tokenPrefix)) {
            return fullToken.substring(tokenPrefix.length() + 1);
        }
        return fullToken;
    }

    /**
     * 获取签名密钥（jjwt 0.11.x 使用 SecretKey）
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 32) {
                sb.append(secret);
            }
            keyBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        }
        byte[] key = new byte[32];
        System.arraycopy(keyBytes, 0, key, 0, 32);
        return Keys.hmacShaKeyFor(key);
    }
}
