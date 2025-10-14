package com.springboot.project.community.service.auth;

import com.springboot.project.community.dto.auth.UserLoginReq;
import com.springboot.project.community.dto.auth.UserRes;
import com.springboot.project.community.dto.auth.UserSignupReq;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     * 1. 이메일과 닉네임 중복을 확인한다.
     * 2. 비밀번호를 암호화하여 저장한다.
     * 3. 저장된 회원 정보를 반환한다.
     */
    @Transactional
    public UserRes signup(UserSignupReq req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByNickname(req.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .image(req.getImage())
                .useYn(false)
                .build();

        userRepository.save(user);

        return UserRes.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    /**
     * 로그인 처리
     * 1. 이메일을 통해 사용자를 조회한다.
     * 2. 비밀번호가 일치하는지 확인한다.
     * 3. 일치하면 사용자 정보를 반환한다.
     */
    @Transactional(readOnly = true)
    public UserRes login(UserLoginReq req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return UserRes.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
