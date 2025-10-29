package com.springboot.project.community.dto.comment;

import com.springboot.project.community.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUpdateReq {
    private Long postId;
    private Long commentId;

    @NotBlank(message = "내용은 반드시 입력해야 합니다.")
    private String contents;

    private String author;
    private LocalDateTime updatedAt;

    public static CommentUpdateReq from(Comment comment) {
        return CommentUpdateReq.builder()
                .postId(comment.getBoard().getPostId()) // 댓글이 속한 게시글 ID
                .commentId(comment.getCommentId())
                .contents(comment.getContents())
                .author(comment.getAuthor().getNickname()) // 작성자 닉네임
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
