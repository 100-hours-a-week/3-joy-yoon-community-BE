package com.springboot.project.community.controller.auth;

import com.springboot.project.community.dto.auth.EmailCheckReq;
import com.springboot.project.community.dto.auth.NicknameCheckReq;
import com.springboot.project.community.dto.auth.PasswordChangeReq;
import com.springboot.project.community.dto.auth.UserLoginReq;
import com.springboot.project.community.dto.auth.UserRes;
import com.springboot.project.community.dto.auth.UserSignupReq;
import com.springboot.project.community.dto.auth.UserUpdateReq;
import com.springboot.project.community.entity.User;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 컨트롤러
 * 
 * 책임: HTTP 요청/응답 처리만 담당
 * - 회원가입, 로그인, 로그아웃
 * - 토큰 재발급, 토큰 유효성 검사
 * - 사용자 정보 조회, 수정, 비밀번호 변경, 회원탈퇴
 * - 이메일/닉네임 중복 검사
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;

    // ==================== 회원가입/로그인/로그아웃 ====================

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody @Valid UserSignupReq req) {
        try {
            log.info("회원가입 요청: email={}, nickname={}", req.getEmail(), req.getNickname());
            UserRes userRes = userService.signup(req);
            log.info("회원가입 성공: userId={}", userRes.getUserId());

            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", userRes.getUserId());
            userMap.put("userId", userRes.getUserId());
            userMap.put("email", userRes.getEmail());
            userMap.put("nickname", userRes.getNickname());
            if (userRes.getImage() != null) {
                userMap.put("image", userRes.getImage());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원가입 성공",
                    "user", userMap
            ));
        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "회원가입 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody @Valid UserLoginReq req,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            log.info("로그인 요청: email={}, rememberMe={}", req.getEmail(), req.isRememberMe());

            // 1. 인증 서비스 호출
            User user = authService.login(
                    req.getEmail(),
                    req.getPassword(),
                    req.isRememberMe(),
                    request,
                    response
            );

            log.info("인증 성공: userId={}, email={}", user.getUserId(), user.getEmail());

            // 2. JWT 토큰 발급
            Map<String, String> tokens = tokenService.issueTokens(user, response);

            log.info("토큰 발급 완료: userId={}", user.getUserId());

            // 3. 프로필 이미지 조회
            String profileImageUrl = user.getImage();

            // 4. 응답
            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", user.getUserId());
            userMap.put("userId", user.getUserId());
            userMap.put("email", user.getEmail());
            userMap.put("nickname", user.getNickname());
            if (profileImageUrl != null) {
                userMap.put("image", profileImageUrl);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "로그인 성공",
                    "accessToken", tokens.get("accessToken"),
                    "tokenType", tokens.get("tokenType"),
                    "user", userMap
            ));
        } catch (IllegalArgumentException e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            e.printStackTrace(); // 스택 트레이스 출력
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "로그인 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @AuthenticationPrincipal String userIdStr,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // 1. JWT 토큰 무효화
            if (userIdStr != null) {
                Long userId;
                try {
                    userId = Long.valueOf(userIdStr);
                    tokenService.logout(userId, response);
                } catch (NumberFormatException e) {
                    log.error("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
                }
            }

            // 2. 세션 + Remember Me 쿠키 무효화
            authService.logout(request, response);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "로그아웃 성공"
            ));
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "로그아웃 중 오류가 발생했습니다."
            ));
        }
    }

    // ==================== 토큰 관리 ====================

    /**
     * Access Token 재발급
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request) {
        try {
            String refreshToken = cookieUtil.getCookieValue(request, "refreshToken")
                    .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 없습니다."));

            Map<String, String> tokens = tokenService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accessToken", tokens.get("accessToken"),
                    "tokenType", tokens.get("tokenType")
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "토큰 갱신 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * Access Token 유효성 검증
     * GET /api/auth/check
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkToken(HttpServletRequest request) {
        boolean isValid = tokenService.validateAccessToken(request);
        return ResponseEntity.ok(Map.of("isValid", isValid));
    }

    // ==================== 사용자 정보 ====================

    /**
     * 현재 로그인 사용자 정보 조회
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal String userIdStr) {
        
        // String으로 받은 userId를 Long으로 변환
        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            log.error("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "인증 오류가 발생했습니다."
            ));
        }

        try {
            log.info("사용자 정보 조회 요청: userId={}", userId);
            UserRes userRes = userService.getUserInfo(userId);

            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", userRes.getUserId());
            userMap.put("userId", userRes.getUserId());
            userMap.put("email", userRes.getEmail());
            userMap.put("nickname", userRes.getNickname());
            if (userRes.getImage() != null) {
                userMap.put("image", userRes.getImage());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "user", userMap
            ));
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "사용자 정보 조회 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 회원정보 수정
     * PUT /api/auth/update
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(
            @AuthenticationPrincipal String userIdStr,
            @RequestBody UserUpdateReq req) {

        // String으로 받은 userId를 Long으로 변환
        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            log.error("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "인증 오류가 발생했습니다."
            ));
        }

        try {
            log.info("회원정보 수정 요청: userId={}, nickname={}, image={}", userId, req.getNickname(), req.getImage() != null ? (req.getImage().length() > 50 ? req.getImage().substring(0, 50) + "..." : req.getImage()) : "없음");
            UserRes userRes = userService.updateUser(userId, req);
            log.info("회원정보 수정 성공: userId={}", userId);

            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", userRes.getUserId());
            userMap.put("userId", userRes.getUserId());
            userMap.put("email", userRes.getEmail());
            userMap.put("nickname", userRes.getNickname());
            if (userRes.getImage() != null) {
                userMap.put("image", userRes.getImage());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원정보가 성공적으로 수정되었습니다.",
                    "user", userMap
            ));
        } catch (IllegalArgumentException e) {
            log.error("회원정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("회원정보 수정 중 오류 발생", e);
            e.printStackTrace(); // 스택 트레이스 출력
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "회원정보 수정 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    // ==================== 중복 검사 ====================

    /**
     * 이메일 중복 검사
     * GET /api/auth/check-email?email=xxx (쿼리 파라미터)
     * GET /api/auth/check-email (body - 비표준이지만 지원)
     * POST /api/auth/check-email (body)
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailGet(
            @RequestParam(required = false) String email,
            @RequestBody(required = false) EmailCheckReq req) {
        
        String emailToCheck = null;
        
        // 쿼리 파라미터 우선, 없으면 body에서 가져오기
        if (email != null && !email.isBlank()) {
            emailToCheck = email;
        } else if (req != null && req.getEmail() != null && !req.getEmail().isBlank()) {
            emailToCheck = req.getEmail();
        }
        
        if (emailToCheck == null || emailToCheck.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이메일은 필수 입력값입니다."
            ));
        }
        
        boolean available = userService.isEmailAvailable(emailToCheck);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "email", emailToCheck,
                "message", available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
        ));
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailPost(@RequestBody @Valid EmailCheckReq req) {
        boolean available = userService.isEmailAvailable(req.getEmail());

        return ResponseEntity.ok(Map.of(
                "available", available,
                "email", req.getEmail(),
                "message", available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
        ));
    }

    /**
     * 닉네임 중복 검사
     * GET /api/auth/check-nickname?nickname=xxx (쿼리 파라미터)
     * GET /api/auth/check-nickname (body - 비표준이지만 지원)
     * POST /api/auth/check-nickname (body)
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNicknameGet(
            @RequestParam(required = false) String nickname,
            @RequestBody(required = false) NicknameCheckReq req) {
        
        String nicknameToCheck = null;
        
        // 쿼리 파라미터 우선, 없으면 body에서 가져오기
        if (nickname != null && !nickname.isBlank()) {
            nicknameToCheck = nickname;
        } else if (req != null && req.getNickname() != null && !req.getNickname().isBlank()) {
            nicknameToCheck = req.getNickname();
        }
        
        if (nicknameToCheck == null || nicknameToCheck.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "닉네임은 필수 입력값입니다."
            ));
        }
        
        boolean available = userService.isNicknameAvailable(nicknameToCheck);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "nickname", nicknameToCheck,
                "message", available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."
        ));
    }

    @PostMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNicknamePost(@RequestBody @Valid NicknameCheckReq req) {
        boolean available = userService.isNicknameAvailable(req.getNickname());

        return ResponseEntity.ok(Map.of(
                "available", available,
                "nickname", req.getNickname(),
                "message", available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다."
        ));
    }

    // ==================== 비밀번호 변경 ====================

    /**
     * 비밀번호 변경
     * PUT /api/auth/change-password
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal String userIdStr,
            @RequestBody @Valid PasswordChangeReq req,
            HttpServletRequest request) {

        log.info("비밀번호 변경 API 호출: path={}, method={}, userIdStr={}, Authorization={}", 
                request.getRequestURI(), request.getMethod(), userIdStr, request.getHeader("Authorization"));

        // 인증 확인
        if (userIdStr == null || userIdStr.isBlank()) {
            log.error("인증된 사용자가 없습니다. SecurityContext: {}", 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        // String으로 받은 userId를 Long으로 변환
        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            log.error("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "인증 오류가 발생했습니다."
            ));
        }

        try {
            log.info("비밀번호 변경 요청: userId={}", userId);
            userService.changePassword(userId, req.getCurrentPassword(), req.getNewPassword());
            log.info("비밀번호 변경 성공: userId={}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "비밀번호가 성공적으로 변경되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.error("비밀번호 변경 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * @ControllerAdvice 대신 개별 컨트롤러에서 유효성 검사 오류 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new java.util.HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Map.of(
                "success", false,
                "message", "유효성 검사 실패",
                "errors", errors
        );
    }
}
