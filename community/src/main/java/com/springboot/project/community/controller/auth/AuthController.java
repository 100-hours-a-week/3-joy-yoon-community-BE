package com.springboot.project.community.controller.auth;

import com.springboot.project.community.dto.auth.TokenRes;
import com.springboot.project.community.dto.auth.UserLoginReq;
import com.springboot.project.community.dto.auth.UserRes;
import com.springboot.project.community.dto.auth.UserSignupReq;
import com.springboot.project.community.repository.UserRepository;
import com.springboot.project.community.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springboot.project.community.entity.User;


/**
 *  인증 관련 컨트롤러
 * - 회원가입 / 로그인 / JWT 발급
 */
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
    public User signup(UserSignupReq req) {

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
     * 로그인 → JWT 토큰 발급
     */
//    @PostMapping("/login")
//    public TokenRes login(@RequestBody @Valid UserLoginReq req) {
//        return authService.login(req);
//    }
}

