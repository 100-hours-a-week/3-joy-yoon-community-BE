package com.springboot.project.community.dto.auth;

import lombok.*;

/**
 *  회원 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRes {
    private Long userId;
    private String email;
    private String nickname;
    private String image;
}
