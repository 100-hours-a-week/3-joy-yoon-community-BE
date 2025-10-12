package com.springboot.project.community.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * email과 password 필드를 포함하며, 둘 다 필수 입력값.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginReq {

    /**
     * 로그인 시 사용되는 이메일 주소.
     * - @Email 검증을 통해 이메일 형식 확인.
     */
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    /**
     * 로그인 시 입력되는 비밀번호.
     * - 평문으로 들어오며, Service 계층에서 PasswordEncoder로 암호화 비교 수행.
     */
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
}
