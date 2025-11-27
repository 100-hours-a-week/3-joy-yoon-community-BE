package com.springboot.project.community.security;

import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *  Spring Security 규약에 맞춘 사용자 조회 서비스
 *
 * - loadUserByUsername(String username)는 통상적으로 "이메일"을 의미하도록 사용하는 것이 일반적입니다.
 * - DB에서 이메일로 User 엔티티를 조회하여 CustomUserDetails로 감싸 반환합니다.
 * - (중요) JWT 필터에서 사용자 식별을 이메일로 수행하도록 맞추면 효율적입니다.
 *   - JwtTokenProvider#getEmail(token)을 사용 → 본 서비스의 loadUserByUsername(email) 호출
 *   - 만약 userId로만 식별하고 싶다면, 별도의 loadById(Long id) 메서드를 가진 도우미 서비스를 두거나,
 *     UserDetailsService 구현을 확장해 커스텀 메서드를 함께 제공하는 패턴을 권장합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     *  이메일을 이용해 사용자 정보를 로드
     * @param email 인증 시 사용되는 이메일(=username)
     * @return UserDetails (CustomUserDetails)
     * @throws UsernameNotFoundException 이메일을 찾지 못한 경우
     */
    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final Optional<User> optionalUser = userRepository.findByEmail(email);
        final User user = optionalUser.orElseThrow(
                () -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email)
        );
        return new CustomUserDetails(user);
        // 이후 SecurityContext 에 저장되는 Principal은 CustomUserDetails 인스턴스가 됩니다.
    }
}

