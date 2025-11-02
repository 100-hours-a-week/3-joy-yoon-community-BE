package com.springboot.project.community.repository;

import com.springboot.project.community.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Refresh Token 리포지토리
 *
 * [향후 JWT 전환 시 사용]
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 사용자 ID로 Refresh Token 조회
     */
    Optional<RefreshToken> findByUserId(Long userId);

    /**
     * 토큰으로 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID로 Refresh Token 삭제
     */
    void deleteByUserId(Long userId);

    /**
     * 사용자 ID로 Refresh Token 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
}
