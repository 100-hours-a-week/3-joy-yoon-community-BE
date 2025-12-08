package com.springboot.project.community.controller.like;

import com.springboot.project.community.dto.like.LikeToggleRes;
import com.springboot.project.community.service.like.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 좋아요 컨트롤러
 * - 좋아요 토글 (누르면 좋아요 / 다시 누르면 취소)
 */
@Slf4j
@RestController
@RequestMapping("/api/boards/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 좋아요 토글 API
     * POST /api/boards/{postId}/likes
     */
    @PostMapping
    public ResponseEntity<LikeToggleRes> toggleLike(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long postId,
            HttpServletRequest request) {
        
        log.info("좋아요 토글 API 호출: path={}, method={}, userIdStr={}, postId={}, Authorization={}", 
                request.getRequestURI(), request.getMethod(), userIdStr, postId, request.getHeader("Authorization"));

        // 인증 확인
        if (userIdStr == null || userIdStr.isBlank()) {
            log.error("인증된 사용자가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // String으로 받은 userId를 Long으로 변환
        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            log.error("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            log.info("좋아요 토글 요청: userId={}, postId={}", userId, postId);
            LikeToggleRes result = likeService.toggle(userId, postId);
            log.info("좋아요 토글 성공: userId={}, postId={}, liked={}", userId, postId, result.isLiked());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("좋아요 토글 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("좋아요 토글 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
