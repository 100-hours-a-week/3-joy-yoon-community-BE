package com.springboot.project.community.controller.auth;

import com.springboot.project.community.dto.auth.UserLoginReq;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.security.jwt.JwtTokenProvider;
import com.springboot.project.community.security.jwt.TokenService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class JwtAuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody UserLoginReq loginRequest,
            HttpServletResponse response) {

        // findByEmail은 이미 User 객체를 반환 (없으면 예외 발생)
        User user = userService.findByEmail(loginRequest.getEmail());

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

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
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 refreshToken 가져오기
        String refreshToken = CookieUtil.getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        // Access Token 재발급
        Map<String, String> tokens = tokenService.refreshAccessToken(refreshToken);

        // 신규 refreshToken 쿠키 재설정
        tokenService.setRefreshTokenCookie(tokens.get("refreshToken"), response);

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
}