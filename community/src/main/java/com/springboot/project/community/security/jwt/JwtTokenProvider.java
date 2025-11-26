package com.springboot.project.community.security.jwt;

import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰 생성 및 검증
 *
 * [향후 JWT 전환 시 사용]
 * - Access Token : 15분, 클라이언트 메모리 저장
 * - Refresh Token : 7일, HttpOnly 쿠키 저장
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final String secret;
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    // 생성자에서 @Value를 필드가 아닌 생성자에 직접 사용
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration:900000}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration) {
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

        // ✅ secretKey 초기화 추가
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     * - 짧은 유효기간 (15분)
     * - 클라이언트 메모리(변수)에 저장
     */
    public String createAccessToken(Long userId, String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     * - 긴 유효기간 (7일)
     * - HttpOnly 쿠키 저장
     */
    public String createRefreshToken(Long userId, String email) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(userId.toString())   // user 식별
                .claim("email", email)           // 이메일 포함 (선택)
                .claim("type", "refresh")        // refresh 토큰임을 명시
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token){
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e){
            log.info("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e){
            log.info("지원하지 않는 토큰입니다.");
        } catch (MalformedJwtException e){
            log.info("잘못된 형식의 토큰입니다.");
        } catch (SecurityException e){
            log.info("시그니처 검증에 실패했습니다.");
        } catch (IllegalArgumentException e){
            log.info("잘못된 토큰입니다.");
        }
        return false;
    }

    /**
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰에서 권한 추출
     */
    public List<String> getRoles(String token) {
        Claims claims = parseClaims(token);
        Object roles = claims.get("roles");

        if (roles == null) {
            return Collections.emptyList();
        }

        if (roles instanceof List) {
            return (List<String>) roles;
        }

        return Collections.emptyList();
    }

    /**
     * JWT 생성시 토큰 권한 포함
     */
    public String generateAccessToken(Long userId, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("roles", roles)  // 권한 정보 추가
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)  // ✅ key → secretKey로 변경
                .compact();
    }

    /**
     * 토큰 타입 확인
     */
    public boolean isAccessToken(String token) {
        Claims claims = parseClaims(token);
        return "access".equals(claims.get("type", String.class));
    }

    public boolean isRefreshToken(String token) {
        Claims claims = parseClaims(token);
        return "refresh".equals(claims.get("type", String.class));
    }
}