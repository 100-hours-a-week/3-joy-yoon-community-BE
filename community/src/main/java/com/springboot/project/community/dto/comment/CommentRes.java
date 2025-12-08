package com.springboot.project.community.dto.comment;

import com.springboot.project.community.entity.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/** 댓글 응답 DTO */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CommentRes {
    private Long commentId;
    private Long postId;
    private String author;           // 닉네임
    private String content;
    private LocalDateTime createdAt;

    public static CommentRes from(Comment c) {
        return CommentRes.builder()
                .commentId(c.getCommentId())
                .postId(c.getBoard().getPostId())
                .author(c.getAuthor().getNickname())
                .content(c.getContents())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

