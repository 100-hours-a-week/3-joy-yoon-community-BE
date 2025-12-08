package com.springboot.project.community.dto.board;

import lombok.*;

/**
 * 게시글 삭제 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDeleteRes {
    private Long postId;
    private Long userId;
    private String message;
}

