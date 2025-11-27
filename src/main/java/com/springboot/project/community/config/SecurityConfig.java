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

                // 인증 규칙 (임시 설정)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/signup",
                                "/api/auth/refresh",
                                "/api/auth/check",
                                "/api/auth/check-email",
                                "/api/auth/check-nickname",
                                "/",
                                "/login",
                                "/signup",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/error"
                        ).permitAll()
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
                .httpBasic(AbstractHttpConfigurer::disable);

        // JWT 필터 추가 위치
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT 기반 인증 (API용)
     * /api/** 경로에만 적용
     */
    @Bean
    @Order(1)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/likes")  // /api/** 경로에만 적용
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
//                        .requestMatchers("/api/**").authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 세션 기반 인증 (웹 UI용)
     * /web/** 또는 다른 모든 경로에 적용
     */
    @Bean
    @Order(2)
    public SecurityFilterChain sessionFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/web/**", "/", "/login", "/signup")  // 웹 경로
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))  // API는 CSRF 제외
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))  // 세션 사용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/web/**").authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/web/auth/login")
                        .defaultSuccessUrl("/web/home")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/web/auth/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

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
