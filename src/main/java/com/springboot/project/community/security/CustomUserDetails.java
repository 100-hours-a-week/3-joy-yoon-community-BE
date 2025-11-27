package com.springboot.project.community.security;

import com.springboot.project.community.entity.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *  도메인 User 엔티티를 Security의 UserDetails로 어댑트하는 클래스
 *  Spring Security가 이해할 수 있는 UserDetails 어댑터
 *
 * - Security는 인증 이후 Authentication.getPrincipal()을 통해 UserDetails를 참조.
 * - 여기서는 도메인 User를 그대로 보관하여 컨트롤러/서비스에서 재사용 가능하게 함.
 * - 권한은 데모 기준으로 ROLE_USER 1개만 부여.
 */
@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    /** 도메인 사용자 엔터티 (이메일, 비밀번호, 상태 등 포함) */
    private final User user;

    /**
     * 부여 권한 목록
     * - 필요 시 DB/enum 기반으로 확장 가능
     */
    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /** 인증용 비밀번호(해시) */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /** 인증용 사용자명(여기서는 이메일 사용) */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /** 계정 만료 정책 (미적용) */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 계정 잠김 정책 (미적용) */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 자격 증명(비밀번호) 만료 정책 (미적용) */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     *  계정 활성화 여부
     * - User.useYn == true 라면 비활성(탈퇴/정지), false 라면 활성
     * - 여기서는 `!useYn`일 때만 활성(true)로 간주
     */
    @Override
    public boolean isEnabled() {
        // useYn == null 인 경우를 대비하여 false 기본값 처리
        final Boolean useYn = user.getUseYn();
        return useYn == null || !useYn;
    }
}

