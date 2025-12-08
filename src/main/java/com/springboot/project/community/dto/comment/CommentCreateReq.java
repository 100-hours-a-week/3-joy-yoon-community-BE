package com.springboot.project.community.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 댓글 작성 요청 DTO
 * - 요청 전용 (응답은 CommentRes 사용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateReq {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String contents;
}
