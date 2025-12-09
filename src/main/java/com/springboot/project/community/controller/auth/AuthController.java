package com.springboot.project.community.controller.auth;

import com.springboot.project.community.dto.auth.UserLoginReq;
import com.springboot.project.community.dto.auth.UserRes;
import com.springboot.project.community.dto.auth.UserSignupReq;
import com.springboot.project.community.dto.auth.UserUpdateReq;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import com.springboot.project.community.service.auth.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 *  인증 관련 컨트롤러
 * - 회원가입 / 로그인 / JWT 발급
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     *  회원가입 기능
     */
    @PostMapping("/signup")
    public User signup(@RequestBody @Valid UserSignupReq req) {
        // 이메일 중복 검사
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(req.getPassword());

        // User 엔티티 생성
        // 프로필 이미지 처리 (빈 문자열이면 null로 저장)
        String image = (req.getImage() != null && !req.getImage().isBlank()) 
                ? req.getImage() 
                : null;
        
        User user = User.builder()
                .email(req.getEmail())
                .password(encodedPassword)
                .nickname(req.getNickname())
                .image(image)
                .useYn(false)
                .build();

        // 저장 후 반환
        return userRepository.save(user);
    }

    /**
     * 회원 정보 수정
     */
    @PutMapping("/update")
    public ResponseEntity<UserRes> updateUser(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UserUpdateReq req) {

        User updatedUser = userService.updateUser(userId, req);
        
        // User 엔티티를 UserRes DTO로 변환 (비밀번호 제외)
        UserRes userRes = UserRes.builder()
                .userId(updatedUser.getUserId())
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .image(updatedUser.getImage())
                .build();
        
        return ResponseEntity.ok(userRes);
    }

    // /**
    //  * 로그인
    //  */
    // @PostMapping("/login")
    // public UserRes login(@RequestBody UserLoginReq req) {
    //     return userService.login(req);  //  반환 타입 일치 (UserRes)
    // }

    /*
    * 닉네임 중복 검사
    */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of("available", available));
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

