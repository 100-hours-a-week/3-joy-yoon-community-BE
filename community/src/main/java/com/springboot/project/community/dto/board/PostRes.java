package com.springboot.project.community.dto.board;

import com.springboot.project.community.dto.comment.CommentRes;
import com.springboot.project.community.entity.Board;
import com.springboot.project.community.entity.BoardImage;
import com.springboot.project.community.entity.BoardStats;
import com.springboot.project.community.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  게시글 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRes {
    private Long postId;

    @NotBlank(message = "제목은 반드시 입력해야 합니다.")
    private String title;

    @NotBlank(message = "내용은 반드시 입력해야 합니다.")
    private String contents;

    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Long viewCount;
    private Long commentCount;
    private String commentContents;

    /** 새 이미지 URL 리스트 (기존 이미지 전부 교체됨) */
    private List<String> imageUrls;
    private List<CommentRes> comments;

    public static PostRes of(Board board, BoardStats stats, List<CommentRes> comments) {
        List<String> imageUrls = null;

        // 이미지 URL 목록 변환
        if (board.getImages() != null && !board.getImages().isEmpty()) {
            imageUrls = board.getImages().stream()
                    .map(BoardImage::getImageUrl)
                    .collect(Collectors.toList());
        }

        return PostRes.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .author(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .likeCount(stats != null ? stats.getLikeCount() : 0L)
                .commentCount(stats != null ? stats.getCommentCount() : 0L)
                .viewCount(stats != null ? stats.getViewCount() : 0L)
                .imageUrls(imageUrls)
                .comments(comments)
                .build();
    }
}
