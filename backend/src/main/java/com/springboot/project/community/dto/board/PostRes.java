package com.springboot.project.community.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<String> imageUrls;

    private String author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
