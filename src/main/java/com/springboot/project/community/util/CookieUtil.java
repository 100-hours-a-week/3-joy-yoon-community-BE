package com.springboot.project.community.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    /**
    * 쿠키 생성
    * @param name 쿠키 이름
    * @param value 쿠키 값
    * @parma maxAge 유효 시간
    */
    public Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // XSS 방지
        cookie.setSecure(false); // 개발환경에서는 false, 프로덕션에서는 true(HTTPS)
        cookie.setPath("/"); // 전체 경로에서 접근 가능
        cookie.setMaxAge(maxAge); // 유효시간 설정
        return cookie;
    }

    /**
    * Refresh Token 용 쿠키 생성 (향후 사용)
    */
    public Cookie createRefreshTokenCookie(String refreshToken){
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // XSS 방지
        cookie.setSecure(false); // 개발환경에서는 false, 프로덕션에서는 true(HTTPS)
        cookie.setPath("/"); // 전체 경로에서 접근 가능
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        return cookie;
    }

    /**
    * 쿠키 조회
    */
    public Optional<Cookie> getCookie(HttpServletRequest request, String name){
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
    * 쿠키 값 조회
    */
    public Optional<String> getCookieValue(HttpServletRequest request, String name) {
        return getCookie(request, name)
                .map(Cookie::getValue);
    }

    /**
     * 쿠키 삭제
     */
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 여러 쿠키 삭제
     */
    public void deleteCookies(HttpServletResponse response, String... names) {
        for (String name : names) {
            deleteCookie(response, name);
        }
    }
}
