package com.springboot.project.community.service.auth;

import com.springboot.project.community.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 세션 관리 서비스
 * - 현재 : HttpSession 기반 인증
 * - 향후 : JWT 기반으로 마이그레이션 시 이 클래스는 점진적으로 제거 예정
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String USER_SESSION_KEY = "loginUser";
    private static final int SESSION_TIMEOUT = 30 * 60; // 30분

    /**
     * 로그인 : 세션에 사용자 정보 저장
     */
    public void login(HttpServletRequest request, User user) {
        HttpSession session = request.getSession();
        session.setAttribute(USER_SESSION_KEY, user);
        session.setMaxInactiveInterval(SESSION_TIMEOUT);
    }

    /**
     * 로그아웃 : 세션 무효화
     */
    public void logout(HttpServletRequest request) {
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

    /**
     * 세션에 임시 데이터 저장
     */
    public void setAttribute(HttpServletRequest request, String key, Object value) {
        HttpSession session = request.getSession();
        session.setAttribute(key, value);
    }

    /**
     * 세션에서 임시 데이터 조회
     */
    public Optional<Object> getAttribute(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return Optional.ofNullable(session.getAttribute(key));
        }
        return Optional.empty();
    }

    /**
     * 세션에서 임시 데이터 제거
     */
    public void removeAttribute(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(key);
        }
    }

}
