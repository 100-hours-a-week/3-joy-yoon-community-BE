package com.springboot.project.community.security.jwt;

import com.springboot.project.community.entity.User;
import com.springboot.project.community.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Access Token과 Refresh Token 발급
     */
    public Map<String, String> issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId(), user.getEmail());

        // Refresh Token을 쿠키에 저장
        setRefreshTokenCookie(refreshToken, response);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("tokenType", "Bearer");

        return tokens;
    }

    /**
     * Refresh Token으로 Access Token 재발급
     */
    public Map<String, String> refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // Refresh Token에서 userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 새로운 Access Token 생성 (email은 null로 전달 - 필수 아님)
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, null);
        
        // 새로운 Refresh Token 생성 (RTR 패턴 - Refresh Token Rotation)
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, null);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        tokens.put("tokenType", "Bearer");

        return tokens;
    }

    /**
     * Refresh Token 쿠키 설정
     */
    public void setRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        response.addCookie(CookieUtil.createRefreshTokenCookie(refreshToken));
    }

    /**
     * 로그아웃
     */
    public void logout(Long userId, HttpServletResponse response) {
        // Refresh Token 쿠키 삭제
        CookieUtil.deleteCookie(response, "refreshToken");
        
        log.info("User {} logged out", userId);
    }
}