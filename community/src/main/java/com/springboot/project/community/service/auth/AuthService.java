package com.springboot.project.community.service.auth;

import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import com.springboot.project.community.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 인증 서비스
 * - 로그인/로그아웃 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final CookieUtil cookieUtil;

    private static final String REMEMBER_ME_COOKIE = "rememberMe";
    private static final int REMEBER_ME_DURATION = 7 * 24 * 60 * 60; // 7일

    /**
     * 로그인
     */
    @Transactional
    public User login(String email, String password, boolean rememberMe,
                      HttpServletRequest request, HttpServletResponse response) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. 세션에 사용자 정보 저장
        sessionService.login(request, user);

        // 4. Remember Me 쿠키 처리
        if (rememberMe) {
            String rememberMeToken = generateRememberMeToken(user);
            Cookie cookie = cookieUtil.createCookie(REMEMBER_ME_COOKIE, rememberMeToken, REMEBER_ME_DURATION);
            response.addCookie(cookie);
        }
        return user;
    }

    /**
     * 로그아웃
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 세션 무효화
        sessionService.logout(request);

        // 2. Remember Me 쿠키 삭제
        cookieUtil.deleteCookie(response, REMEMBER_ME_COOKIE);
    }

    /**
     * Remember Me 토큰 생성
     * 향후 JWT 기반으로 전환 시 이 부분이 Refresh Token 생성으로 변경됨
     */
    public String generateRememberMeToken(User user) {
        // 현재는 간단히 구현
        // 실제로는 UUID + 사용자ID + 만료시간 조합 후 암호화
        return user.getUserId() + ":" + System.currentTimeMillis();
    }

    /**
     * Remember Me 토큰 검증 및 자동 로그인
     */
    public boolean autoLoginByRememberMe(HttpServletRequest request, HttpServletResponse response) {
        return cookieUtil.getCookieValue(request, REMEMBER_ME_COOKIE)
                .flatMap(this::validateAndParseToken)
                .map(userId -> {
                    User user = userRepository.findById(userId)
                            .orElse(null);
                    if (user != null){
                        sessionService.login(request, user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Remember Me 토큰 파싱 및 검증
     */
    public Optional<Long> validateAndParseToken(String token) {
        try {
            String[] parts = token.split(":");
            if (parts.length == 2) {
                Long userId = Long.parseLong(parts[0]);
                Long timestamp = Long.parseLong(parts[1]);

                // 만료 시간 체크 (7일)
                if (System.currentTimeMillis() - timestamp < REMEBER_ME_DURATION * 1000L) {
                    return Optional.of(userId);
                }
            }
        } catch (Exception e) {
            // 토큰 파싱 실패
        }
        return Optional.empty();
    }

}
