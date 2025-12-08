package com.springboot.project.community.config;

import com.springboot.project.community.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정 클래스
 * - CSRF 비활성화
 * - CORS 정책 명시적 설정 (localhost:3000 허용)
 * - 모든 요청 인증 없이 허용 (추후 인증 정책 추가 가능)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 보안 필터 체인 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 정책 : 현재 세션 사용
                // 향후 JWT로 전환시 SessionCrestionPolicy.STATELESS로 변경
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1) // 동시 세션 1개로 제한
                        .maxSessionsPreventsLogin(false) // 새 로그인 시 기존 세션 만료
                )

                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 인증 규칙
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능
                        .requestMatchers(
                                // 인증 API
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/check",
                                "/api/auth/check-email",
                                "/api/auth/check-nickname",
                                // 정적 리소스
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/error"
                        ).permitAll()
                        // 게시판 조회만 비로그인 허용 (GET만)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/boards").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/boards/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/boards/*/comments").permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 로그아웃 설정
                .logout(logout -> logout
//                        .logoutUrl("/api/v1/auth/logout")
//                        .logoutSuccessUrl("/api/v1/auth/check")
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/api/auth/check")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "rememberMe")
                )

                // 기본 로그인/HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 정책 설정
     * - 프론트엔드(localhost:3000) 요청 허용
     * - allowCredentials(true) 시 allowedOriginPatterns 사용 필수
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
