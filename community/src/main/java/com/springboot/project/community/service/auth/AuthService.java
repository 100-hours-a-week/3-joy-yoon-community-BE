package com.springboot.project.community.service.auth;


import com.springboot.project.community.dto.auth.*;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
//import com.springboot.project.community.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; // 주석 처리

    public void register(User user) {
        // user.setPassword(passwordEncoder.encode(user.getPassword())); // 주석 처리
        userRepository.save(user);
    }
}


///** 회원가입 및 로그인 서비스 */
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtTokenProvider jwtTokenProvider;
//
//    /** 회원가입 */
//    @Transactional
//    public UserRes signup(UserSignupReq req) {
//        if (userRepository.existsByEmail(req.email())) throw new IllegalArgumentException("이메일 중복");
//        if (userRepository.existsByNickname(req.nickname())) throw new IllegalArgumentException("닉네임 중복");
//
//        User user = User.builder()
//                .email(req.email())
//                .password(passwordEncoder.encode(req.password()))
//                .nickname(req.nickname())
//                .image(req.image())
//                .useYn(false)
//                .build();
//        userRepository.save(user);
//
//        return UserRes.builder()
//                .userId(user.getUserId())
//                .email(user.getEmail())
//                .nickname(user.getNickname())
//                .build();
//    }
//
//    /** 로그인 */
//    @Transactional(readOnly = true)
//    public TokenRes login(UserLoginReq req) {
//        User user = userRepository.findByEmail(req.email())
//                .orElseThrow(() -> new IllegalArgumentException("이메일 없음"));
//        if (!passwordEncoder.matches(req.password(), user.getPassword()))
//            throw new IllegalArgumentException("비밀번호 불일치");
//
//        String token = jwtTokenProvider.createToken(user.getUserId(), user.getEmail());
//        return TokenRes.builder()
//                .accessToken(token)
//                .tokenType("Bearer")
//                .userId(user.getUserId())
//                .email(user.getEmail())
//                .nickname(user.getNickname())
//                .build();
//    }
//}
