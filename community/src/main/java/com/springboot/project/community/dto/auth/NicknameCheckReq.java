package com.springboot.project.community.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 닉네임 중복 검사 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NicknameCheckReq {
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;
}

