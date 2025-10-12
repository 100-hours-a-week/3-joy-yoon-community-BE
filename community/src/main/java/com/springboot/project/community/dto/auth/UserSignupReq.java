package com.springboot.project.community.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 이 클래스는 Controller → Service 계층으로 전달되어
 * 실제 회원가입 로직(User 생성)에 사용됩니다.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupReq {

    /**
     * - 반드시 존재해야 하며, 이메일 형식(@ 포함)을 만족해야함.
     * - unique 제약 조건이 USERS 테이블의 email 컬럼에 적용.
     */
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    /**
     * 사용자의 비밀번호.
     * - 필수 입력값이며 8자 이상이어야 합니다.
     * - PasswordEncoder를 통해 암호화되어 DB에 저장됩니다.
     */
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    /**
     * 사용자의 닉네임.
     * - 중복되지 않아야 하며, 비속어 필터링 등의 추가 검증이 서비스 레벨에서 가능합니다.
     */
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;

    /**
     * 사용자의 프로필 이미지 경로 또는 URL.
     * - 선택값으로, 기본 프로필 이미지가 설정될 수 있습니다.
     */
    private String image;
}