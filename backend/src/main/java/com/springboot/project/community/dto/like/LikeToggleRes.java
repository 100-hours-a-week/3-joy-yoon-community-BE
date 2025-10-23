package com.springboot.project.community.dto.like;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  좋아요 토글 응답 DTO
 * - 사용자가 좋아요를 눌렀는지 여부와 현재 좋아요 수를 반환
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeToggleRes {
    private Long postId;     // 게시글 ID
    private Long likeCount;  // 현재 좋아요 수
    private boolean liked;   // 내가 좋아요 눌렀는지 여부
}
