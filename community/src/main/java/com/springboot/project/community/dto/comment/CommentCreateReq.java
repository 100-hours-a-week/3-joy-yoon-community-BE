package com.springboot.project.community.dto.comment;

import com.springboot.project.community.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/** 댓글 작성 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateReq {
    private Long commentId;
    private Long postId;
    private String author;           // 닉네임
    private String contents;
    private LocalDateTime createdAt;

    public static CommentCreateReq from(Comment c) {
        return CommentCreateReq.builder()
                .commentId(c.getCommentId())
                .postId(c.getBoard().getPostId())
                .author(c.getAuthor().getNickname())
                .contents(c.getContents())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
