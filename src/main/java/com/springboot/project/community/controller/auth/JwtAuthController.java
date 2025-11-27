package com.springboot.project.community.controller.auth;

import com.springboot.project.community.dto.auth.UserLoginReq;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.security.jwt.JwtTokenProvider;
import com.springboot.project.community.security.jwt.TokenService;
import com.springboot.project.community.service.auth.AuthService;
import com.springboot.project.community.service.auth.UserService;
import com.springboot.project.community.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtAuthController {

    private final UserService userService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody UserLoginReq loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        // findByEmail은 이미 User 객체를 반환 (없으면 예외 발생)
        // User user = authService.findByEmail(loginRequest.getEmail());
        User user = authService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.isRememberMe(),
                request,
                response
        );
//        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
//            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
//        }

        // 2. 토큰 발급
        Map<String, String> tokens = tokenService.issueTokens(user, response);

        // 3. 응답
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "로그인 성공");
        result.put("accessToken", tokens.get("accessToken"));
        result.put("tokenType", tokens.get("tokenType"));
        result.put("user", Map.of(
                "id", user.getUserId(),
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request) {
        String refreshToken = cookieUtil.getCookieValue(request, "refreshToken")
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 없습니다."));

        Map<String, String> tokens = tokenService.refreshAccessToken(refreshToken);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("accessToken", tokens.get("accessToken"));
        result.put("tokenType", tokens.get("tokenType"));

        return ResponseEntity.ok(result);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response) {

        tokenService.logout(userId, response);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "로그아웃 성공");

        return ResponseEntity.ok(result);
    }

    /**
     * 현재 로그인 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal Long userId) {

        // findById는 이미 User 객체를 반환 (없으면 예외 발생)
        User user = userService.findById(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("user", Map.of(
                "id", user.getUserId(),
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));

        return ResponseEntity.ok(result);
    }

    /**
     * Access Token 검증
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkToken(
            HttpServletRequest request) {

        String accessToken = extractAccessToken(request);
        boolean isValid = accessToken != null && jwtTokenProvider.validateToken(accessToken);

        Map<String, Object> result = new HashMap<>();
        result.put("isValid", isValid);

        return ResponseEntity.ok(result);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /*
     * 이메일 중복 검사
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /*
     * 이메일 중복 검사 JWT
     */
    @GetMapping("/check-email-jwt")
    public ResponseEntity<Map<String, Object>> checkEmailJwt(@RequestParam String email) {
        boolean isAvailable = authService.findByEmailOptional(email).isEmpty();

        Map<String, Object> result = Map.of(
                "available", isAvailable,
                "email", email,
                "message", isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
        );

        return ResponseEntity.ok(result);
    }
}