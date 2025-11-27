package com.springboot.project.community.controller.auth;

import com.springboot.project.community.dto.auth.*;
import com.springboot.project.community.repository.UserRepository;
import com.springboot.project.community.service.auth.AuthService;
import com.springboot.project.community.service.auth.SessionService;
import com.springboot.project.community.service.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.springboot.project.community.entity.User;

import java.util.HashMap;
import java.util.Map;


/**
 *  인증 관련 컨트롤러
 * - 회원가입 / 로그인 / JWT 발급
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    private final SessionService sessionService;

    /**
     * 로그인
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody UserLoginReq loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        User user = authService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.isRememberMe(),
                request,
                response
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "로그인 성공");
        result.put("user", Map.of(
                "id", user.getUserId(),
                "email", user.getEmail(),
                "nickname", user.getNickname()
        ));
        return ResponseEntity.ok(result);
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        authService.logout(request, response);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "로그아웃 성공");

        return ResponseEntity.ok(result);
    }

    /**
     * 현재 로그인 사용자 정보 조회
     * GET /api/v1/auth
     */
    @GetMapping
    public ResponseEntity<?> getCurrenUser(HttpServletRequest request) {
        return sessionService.getLoginUser(request)
                .map(user -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("user", Map.of(
                            "id", user.getUserId(),
                            "email", user.getEmail(),
                            "nickname", user.getNickname()
                    ));
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.status(401)
                        .body(Map.of(
                                "success", false,
                                "message", "로그인이 필요합니다."
                        )));
    }

    /**
     * 세션 확인
     * GET /api/v1/auth/check
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(HttpServletRequest request) {
        boolean isLoggedIn = sessionService.isLoggedIn(request);

        Map<String, Object> result = new HashMap<>();
        result.put("isLoggedIn", isLoggedIn);

        return ResponseEntity.ok(result);
    }

    /*
     * 이메일 중복 검사
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean available = userService.isNicknameAvailable(email);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
