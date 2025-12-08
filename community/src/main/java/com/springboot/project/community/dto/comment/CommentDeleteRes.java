package com.springboot.project.community.dto.comment;

import lombok.*;

/**
 * 댓글 삭제 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDeleteRes {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String message;
}

