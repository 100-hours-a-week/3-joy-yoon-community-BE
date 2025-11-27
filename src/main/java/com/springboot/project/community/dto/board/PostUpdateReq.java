package com.springboot.project.community.dto.board;

import com.springboot.project.community.entity.Board;
import com.springboot.project.community.entity.BoardStats;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateReq {
    private Long postId;

    @NotBlank(message = "제목은 반드시 입력해야 합니다.")
    private String title;

    @NotBlank(message = "내용은 반드시 입력해야 합니다.")
    private String contents;

    /** 새 이미지 URL 리스트 (기존 이미지 전부 교체됨) */
    private List<String> imageUrls;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostUpdateReq from(Board board) {
        return PostUpdateReq.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .imageUrls(imageUrls)
                .author(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
