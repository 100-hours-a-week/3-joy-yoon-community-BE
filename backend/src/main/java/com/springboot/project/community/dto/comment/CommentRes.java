package com.springboot.project.community.dto.comment;

import lombok.Builder;
import java.time.LocalDateTime;

/** 댓글 응답 DTO */
@Builder
public record CommentRes(Long commentId, Long postId, String author, String contents, LocalDateTime createdAt) {}
