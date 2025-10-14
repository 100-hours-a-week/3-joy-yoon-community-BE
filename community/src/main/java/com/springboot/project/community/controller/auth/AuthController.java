package com.springboot.project.community.controller.auth;

import com.springboot.project.community.dto.auth.*;
import com.springboot.project.community.repository.UserRepository;
import com.springboot.project.community.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.springboot.project.community.entity.User;


/**
 *  인증 관련 컨트롤러
 * - 회원가입 / 로그인 / JWT 발급
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     *  회원가입 기능
     */
    @PostMapping
    public User signup(@RequestBody @Valid UserSignupReq req) {
        // 이메일 중복 검사
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(req.getPassword());

        // User 엔티티 생성
        User user = User.builder()
                .email(req.getEmail())
                .password(encodedPassword)
                .nickname(req.getNickname())
                .image(req.getImage())
                .useYn(false)
                .build();

        // 저장 후 반환
        return userRepository.save(user);
    }
    /**
     * 회원 정보 수정
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateReq req) {

        User updatedUser = authService.updateUser(userId, req);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public UserRes login(@RequestBody UserLoginReq req) {
        return authService.login(req);  //  반환 타입 일치 (UserRes)
    }

}

