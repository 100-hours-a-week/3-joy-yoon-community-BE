package com.springboot.project.community.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 인증 필터
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        // **수정된 로직:** 인증이 필요 없는 경로는 필터 건너뛰기
        if (shouldSkipFilter(request, requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getJwtFromRequest(request);            
            log.info("JWT 필터 실행: path={}, method={}, token 존재={}", requestPath, request.getMethod(), token != null);

            if (token != null) {
                boolean isValid = jwtTokenProvider.validateToken(token);
                log.info("토큰 검증 결과: path={}, valid={}", requestPath, isValid);
                
                if (isValid) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);
                    log.info("토큰 검증 성공: userId={}", userId);

                    // JWT에서 권한 추출
                    List<String> roles = jwtTokenProvider.getRoles(token);
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // 권한이 없으면 기본 권한 부여
                    if (authorities.isEmpty()) {
                        authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_USER")
                        );
                    }

                    // **핵심 수정:** Principal을 Long에서 String으로 변환하여 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, authorities); // Long -> String 변환

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("인증 정보 설정 완료: userId={}, authorities={}", userId, authorities);
                } else {
                    log.warn("토큰이 유효하지 않음: path={}", requestPath);
                    // SecurityContext를 명시적으로 비움
                    SecurityContextHolder.clearContext();
                }
            } else {
                log.warn("토큰이 없음: path={}", requestPath);
                // SecurityContext를 명시적으로 비움
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            log.error("인증 설정 중 오류 발생: path={}", requestPath, e);
            // 에러 발생 시 SecurityContext 비움
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 필터를 건너뛸 경로 확인 (HTTP 메소드 기반으로 수정)
     */
    private boolean shouldSkipFilter(HttpServletRequest request, String path) {
        // 1. 인증이 필요 없는 API만 건너뛰기
        if (path.equals("/api/auth/signup") ||
            path.equals("/api/auth/login") ||
            path.equals("/api/auth/refresh") ||
            path.equals("/api/auth/check") ||
            path.equals("/api/auth/check-email") ||
            path.equals("/api/auth/check-nickname") ||
            path.startsWith("/error") ||
            path.equals("/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/")) {
            return true;
        }

        // 2. 게시판 조회(GET)만 필터를 건너뛰도록 허용
        if (path.startsWith("/api/boards") && request.getMethod().equals("GET")) {
            if (path.matches("^/api/boards$|^/api/boards/\\d+$|^/api/boards/\\d+/comments$")) {
                return true;
            }
        }
        
        // 3. 인증이 필요한 API는 필터 실행 (JWT 토큰 검증)
        // /api/auth/update, /api/auth/change-password, /api/auth/me, /api/auth/logout 등
        return false;
    }

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}