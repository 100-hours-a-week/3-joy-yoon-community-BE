package com.springboot.project.community.service.auth;

import com.springboot.project.community.dto.auth.UserLoginReq;
import com.springboot.project.community.dto.auth.UserRes;
import com.springboot.project.community.dto.auth.UserSignupReq;
import com.springboot.project.community.dto.auth.UserUpdateReq;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
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

    /**
     * 회원정보 수정
     */
    @Transactional
    public User updateUser(Long userId, UserUpdateReq req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            user.setNickname(req.getNickname());
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        return user;
    }

    /**
     * 닉네임 사용 가능 여부 확인
     */
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * 이메일 사용 가능 여부 확인
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 이메일로 사용자 조회 (User 반환, 없으면 예외)
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
    }

    /**
     * ID로 사용자 조회 (User 반환, 없으면 예외)
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + id));
    }

    /**
     * 이메일로 사용자 조회 (Optional 반환) - JWT Controller용
     */
    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * ID로 사용자 조회 (Optional 반환) - JWT Controller용
     */
    public Optional<User> findByIdOptional(Long id) {
        return userRepository.findById(id);
    }
}