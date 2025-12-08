package com.springboot.project.community.service.auth;

import com.springboot.project.community.dto.auth.UserRes;
import com.springboot.project.community.dto.auth.UserSignupReq;
import com.springboot.project.community.dto.auth.UserUpdateReq;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관리 서비스
 * - 회원가입, 회원조회, 회원정보 수정, 중복 검사
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public UserRes signup(UserSignupReq req) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(req.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname())
                .image(req.getImage())
                .useYn(false) // 활성화 상태로 생성
                .build();

        User savedUser = userRepository.save(user);

        return UserRes.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .nickname(savedUser.getNickname())
                .image(savedUser.getImage())
                .build();
    }

    /**
     * 회원정보 수정
     */
    @Transactional
    public UserRes updateUser(Long userId, UserUpdateReq req) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

            if (req.getNickname() != null && !req.getNickname().isBlank()) {
                // 닉네임 변경 시 중복 검사
                if (!user.getNickname().equals(req.getNickname()) 
                        && userRepository.existsByNickname(req.getNickname())) {
                    throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
                }
                user.setNickname(req.getNickname());
            }

            // 패스워드는 별도 엔드포인트(/api/auth/change-password)로 변경하므로 여기서는 처리하지 않음

            // 이미지 처리: null이거나 빈 문자열이면 삭제, 그 외는 업데이트
            if (req.getImage() != null) {
                if (req.getImage().isBlank()) {
                    // 이미지 삭제 요청: 기존 이미지를 null로 설정
                    user.setImage(null);
                } else {
                    // 새로운 이미지 업데이트
                    user.setImage(req.getImage());
                }
            }

            // 명시적으로 저장 (LONGTEXT 타입의 경우 더티 체킹이 제대로 작동하지 않을 수 있음)
            User savedUser = userRepository.save(user);

            // 프로필 이미지 조회 (users.image 우선 사용)
            String profileImageUrl = savedUser.getImage();

            return UserRes.builder()
                    .userId(savedUser.getUserId())
                    .email(savedUser.getEmail())
                    .nickname(savedUser.getNickname())
                    .image(profileImageUrl)
                    .build();
        } catch (Exception e) {
            // 로깅 추가
            System.err.println("회원정보 수정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ==================== 조회 메서드 ====================

    /**
     * ID로 사용자 조회
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 이메일로 사용자 조회
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 사용자 정보 조회 (UserRes 반환)
     */
    public UserRes getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserRes.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .image(user.getImage())
                .build();
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 현재 비밀번호 확인
        boolean matches = passwordEncoder.matches(currentPassword, user.getPassword());
        if (!matches) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 현재 비밀번호가 같은지 확인
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 새 비밀번호로 변경
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user); // 명시적으로 저장
    }

    // ==================== 중복 검사 메서드 ====================

    /**
     * 이메일 사용 가능 여부 확인
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 사용 가능 여부 확인
     */
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}