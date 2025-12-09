package com.springboot.project.community.config;

import com.springboot.project.community.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

    /**
     * 인터셉터 등록
     * 특정 경로에만 인증 체크 적용
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**") // /api/** 경로에 적용
                .excludePathPatterns(
                        // 인증 관련 (로그인 없이 접근 가능)
                        "/api/auth/signup",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/check",
                        "/api/auth/check-email",
                        "/api/auth/check-nickname",
                        // 인증이 필요한 API는 JWT 필터가 처리하도록 제외
                        "/api/auth/change-password",
                        "/api/auth/me",
                        "/api/auth/logout",
                        "/api/auth/withdraw",
                        // 게시판 조회만 비로그인 허용 (GET만)
                        // POST/PUT/DELETE는 인터셉터가 JWT 토큰을 체크함
                        // 에러 페이지
                        "/error"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*");
    }
}