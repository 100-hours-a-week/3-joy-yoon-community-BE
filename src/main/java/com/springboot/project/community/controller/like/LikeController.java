package com.springboot.project.community.controller.like;

import com.springboot.project.community.dto.like.LikeToggleRes;
import com.springboot.project.community.service.like.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  좋아요 관련 컨트롤러
 * - 좋아요 토글 (누르면 좋아요 / 다시 누르면 취소)
 */
@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 좋아요 토글 API
     */
    @PostMapping("/{postId}")
    public LikeToggleRes toggleLike(
            @AuthenticationPrincipal(expression = "user.userId") Long userId,
            @PathVariable Long postId) {
        return likeService.toggle(userId, postId);
    }
}
