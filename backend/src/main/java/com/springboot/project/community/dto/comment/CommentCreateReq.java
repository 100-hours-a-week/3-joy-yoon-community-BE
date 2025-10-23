package com.springboot.project.community.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/** 댓글 작성 요청 DTO */
@Builder
public record CommentCreateReq(@NotNull Long postId, @NotBlank String contents) {}
