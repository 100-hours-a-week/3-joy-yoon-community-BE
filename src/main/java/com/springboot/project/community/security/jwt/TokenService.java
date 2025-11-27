package com.springboot.project.community.security.jwt;

import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import com.springboot.project.community.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 토큰 관리 서비스
 *
 * [향후 JWT 전환 시 사용]
 * - Access Token 생성 및 반환 (클라이언트가 메모리에 저장)
 * - Refresh Token 생성 및 쿠키 저장
 * - Refresh Token으로 Access Token 재발급
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;
//    private final RefreshTokenRepository refreshTokenRepository; // 향후 추가

    /**
     * 로그인 성공 시 토큰 발급
     * - Access Token : 응답 본문으로 반환 (클라이언트가 메모리에 저장)
     * - Refresh Token : HttpOnly 쿠키로 저장
     */
    @Transactional
    public Map<String, String> issueTokens(User user, HttpServletResponse response) {
        // 1. Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getEmail());

        // 2. Refresh Token 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        // 3. Refresh Token을 쿠키에 저장
        Cookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        // 4. Refresh Token을 DB에 저장 (향후 추가)
        // saveRefreshToken(user.getUserId(), refreshToken);

        // 5. Access Token만 응답으로 반환
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("tokenType", "Bearer");

        return tokens;
    }

    /**
     * Refresh Token으로 Access Token 재발급
     *
     * 클라이언트가 Access Token 만료시:
     * 1. 쿠키의 Refresh Token으로 재발급 요청
     * 2. 새로운 Access Token 반환
     * 3. 클라이언트가 메모리에 저장
     */
    @Transactional
    public Map<String, String> refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 아닙니다.");
        }

        // 2. 사용자 정보 조회
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. DB의 Refresh Token과 일치하는지 확인 (향후 추가)
        // validateRefreshToken(userId, refreshToken);

        // 4. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getEmail());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("tokentype", "Bearer");

        return tokens;
    }

    /**
     * 로그아웃
     * - Refresh Token 쿠키 삭제
     * - DB의 Refresh Token 삭제 (향후 추가)
     */
    @Transactional
    public void logout(Long userId, HttpServletResponse response) {
        // 1. Refresh Token 쿠키 삭제
        cookieUtil.deleteCookie(response, "refreshToken");

        // 2. DB의 Refresh Token 삭제 (향후 추가)
        // deleteRefreshToken(userId);
    }

    // 향후 구현예정
    /*
    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(entity);
    }

    private void validateRefreshToken(Long userId, String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh Token이 없습니다."));

        if (!stored.getToken().equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        if (stored.isExpired()) {
            throw new IllegalArgumentException("만료된 Refresh Token입니다.");
        }
    }

    private void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
    */
}
