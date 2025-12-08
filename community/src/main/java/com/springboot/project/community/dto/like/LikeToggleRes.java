package com.springboot.project.community.dto.like;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("postId")
    private Long postId;     // 게시글 ID
    
    @JsonProperty("likeCount")
    private Long likeCount;  // 현재 좋아요 수
    
    @JsonProperty("isLiked")
    private boolean liked;   // 내가 좋아요 눌렀는지 여부 (JSON에서는 isLiked로 직렬화)
}
