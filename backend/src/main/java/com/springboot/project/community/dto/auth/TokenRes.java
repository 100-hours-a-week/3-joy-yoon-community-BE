package com.springboot.project.community.dto.auth;

import lombok.Builder;

/** 로그인 시 JWT 반환 DTO */
@Builder
public record TokenRes(String accessToken, String tokenType, Long userId, String email, String nickname) {}
