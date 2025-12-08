package com.springboot.project.community.controller.comment;

import com.springboot.project.community.dto.comment.*;
import com.springboot.project.community.service.comment.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 댓글 컨트롤러
 * - 댓글 CRUD
 */
@Slf4j
@RestController
@RequestMapping("/api/boards/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     * POST /api/boards/{postId}/comments
     */
    @PostMapping
    public ResponseEntity<CommentRes> createComment(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateReq req,
            HttpServletRequest request) {
        
        log.info("댓글 작성 API 호출: path={}, method={}, userIdStr={}, postId={}, Authorization={}", 
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
            log.info("댓글 작성 요청: userId={}, postId={}", userId, postId);
            CommentRes comment = commentService.create(userId, postId, req);
            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            log.error("댓글 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("댓글 작성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 댓글 수정
     * PUT /api/boards/{postId}/comments/{commentId}
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentRes> updateComment(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateReq req,
            HttpServletRequest request) {
        
        log.info("댓글 수정 API 호출: path={}, method={}, userIdStr={}, postId={}, commentId={}, Authorization={}", 
                request.getRequestURI(), request.getMethod(), userIdStr, postId, commentId, request.getHeader("Authorization"));

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
            log.info("댓글 수정 요청: userId={}, postId={}, commentId={}", userId, postId, commentId);
            CommentRes comment = commentService.update(userId, postId, commentId, req);
            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            log.error("댓글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("댓글 수정 권한 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("댓글 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 댓글 삭제
     * DELETE /api/boards/{postId}/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentDeleteRes> deleteComment(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            HttpServletRequest request) {
        
        log.info("댓글 삭제 API 호출: path={}, method={}, userIdStr={}, postId={}, commentId={}, Authorization={}", 
                request.getRequestURI(), request.getMethod(), userIdStr, postId, commentId, request.getHeader("Authorization"));

        // 인증 확인
        if (userIdStr == null || userIdStr.isBlank()) {
            log.error("인증된 사용자가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommentDeleteRes.builder()
                    .commentId(commentId)
                    .postId(postId)
                    .userId(null)
                    .message("로그인이 필요합니다.")
                    .build());
        }

        // String으로 받은 userId를 Long으로 변환
        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            log.error("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CommentDeleteRes.builder()
                    .commentId(commentId)
                    .postId(postId)
                    .userId(null)
                    .message("인증 오류가 발생했습니다.")
                    .build());
        }

        try {
            log.info("댓글 삭제 요청: userId={}, postId={}, commentId={}", userId, postId, commentId);
            CommentDeleteRes result = commentService.delete(userId, postId, commentId);
            log.info("댓글 삭제 성공: userId={}, postId={}, commentId={}", userId, postId, commentId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("댓글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(CommentDeleteRes.builder()
                    .commentId(commentId)
                    .postId(postId)
                    .userId(userId)
                    .message(e.getMessage())
                    .build());
        } catch (RuntimeException e) {
            log.error("댓글 삭제 권한 오류: userId={}, postId={}, commentId={}, error={}", userId, postId, commentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CommentDeleteRes.builder()
                    .commentId(commentId)
                    .postId(postId)
                    .userId(userId)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("댓글 삭제 중 오류 발생: userId={}, postId={}, commentId={}", userId, postId, commentId, e);
            return ResponseEntity.internalServerError().body(CommentDeleteRes.builder()
                    .commentId(commentId)
                    .postId(postId)
                    .userId(userId)
                    .message("댓글 삭제 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     * GET /api/boards/{postId}/comments
     */
    @GetMapping
    public ResponseEntity<List<CommentRes>> getComments(@PathVariable Long postId) {
        try {
            List<CommentRes> comments = commentService.findByPost(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("댓글 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
