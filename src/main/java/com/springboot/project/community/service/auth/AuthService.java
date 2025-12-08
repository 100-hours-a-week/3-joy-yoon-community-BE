package com.springboot.project.community.service.auth;

import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import com.springboot.project.community.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 인증 서비스
 * 
 * 책임:
 * - 로그인 검증 (이메일/비밀번호)
 * - 세션 관리 (생성/무효화/조회)
 * - Remember Me 쿠키 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieUtil cookieUtil;

    // 세션 관련 상수
    private static final String USER_SESSION_KEY = "loginUser";
    private static final int SESSION_TIMEOUT = 30 * 60; // 30분

    // Remember Me 관련 상수
    private static final String REMEMBER_ME_COOKIE = "rememberMe";
    private static final int REMEMBER_ME_DURATION = 7 * 24 * 60 * 60; // 7일

    // ==================== 로그인/로그아웃 ====================

    /**
     * 로그인
     * 1. 이메일/비밀번호 검증
     * 2. 세션 생성
     * 3. Remember Me 쿠키 설정 (선택)
     */
    @Transactional
    public User login(String email, String password, boolean rememberMe,
                      HttpServletRequest request, HttpServletResponse response) {
        // 1. 자격 증명 검증
        User user = validateCredentials(email, password);

        // 2. 세션 생성
        createSession(request, user);

        // 3. Remember Me 처리
        if (rememberMe) {
            setRememberMeCookie(user, response);
        }

        return user;
    }

    /**
     * 로그아웃
     * - 세션 무효화
     * - Remember Me 쿠키 삭제
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        invalidateSession(request);
        cookieUtil.deleteCookie(response, REMEMBER_ME_COOKIE);
    }

    /**
     * 이메일/비밀번호 검증
     */
    public User validateCredentials(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    // ==================== 세션 관리 ====================

    /**
     * 세션 생성 및 사용자 정보 저장
     */
    private void createSession(HttpServletRequest request, User user) {
        HttpSession session = request.getSession();
        session.setAttribute(USER_SESSION_KEY, user);
        session.setMaxInactiveInterval(SESSION_TIMEOUT);
    }

    /**
     * 세션 무효화
     */
    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * 현재 로그인한 사용자 조회
     */
    public Optional<User> getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object user = session.getAttribute(USER_SESSION_KEY);
            if (user instanceof User) {
                return Optional.of((User) user);
            }
        }
        return Optional.empty();
    }

    /**
     * 로그인 여부 확인
     */
    public boolean isLoggedIn(HttpServletRequest request) {
        return getLoginUser(request).isPresent();
    }

    // ==================== Remember Me ====================

    /**
     * Remember Me 쿠키 설정
     */
    private void setRememberMeCookie(User user, HttpServletResponse response) {
        String token = generateRememberMeToken(user);
        Cookie cookie = cookieUtil.createCookie(REMEMBER_ME_COOKIE, token, REMEMBER_ME_DURATION);
        response.addCookie(cookie);
    }

    /**
     * Remember Me 토큰 생성
     */
    private String generateRememberMeToken(User user) {
        return user.getUserId() + ":" + System.currentTimeMillis();
    }

    /**
     * Remember Me 토큰으로 자동 로그인
     */
    public boolean autoLoginByRememberMe(HttpServletRequest request) {
        return cookieUtil.getCookieValue(request, REMEMBER_ME_COOKIE)
                .flatMap(this::parseRememberMeToken)
                .map(userId -> {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        createSession(request, user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Remember Me 토큰 파싱 및 검증
     */
    private Optional<Long> parseRememberMeToken(String token) {
        try {
            String[] parts = token.split(":");
            if (parts.length == 2) {
                Long userId = Long.parseLong(parts[0]);
                Long timestamp = Long.parseLong(parts[1]);

                // 만료 시간 체크
                if (System.currentTimeMillis() - timestamp < REMEMBER_ME_DURATION * 1000L) {
                    return Optional.of(userId);
                }
            }
        } catch (Exception ignored) {
            // 토큰 파싱 실패
        }
        return Optional.empty();
    }
}
