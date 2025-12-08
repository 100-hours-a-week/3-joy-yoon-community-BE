package com.springboot.project.community.controller.board;

import com.springboot.project.community.dto.board.*;
import com.springboot.project.community.service.board.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 컨트롤러
 * - 게시글 CRUD
 */
@Slf4j
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 작성
     * POST /api/boards
     */
    @PostMapping
    public ResponseEntity<PostRes> createPost(
            @AuthenticationPrincipal String userIdStr,
            @RequestBody @Valid PostCreateReq req) {

        // String으로 받은 userId를 Long으로 변환
        Long userId;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            log.error("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
        }
        
        try {
            log.info("게시글 작성 요청: userId={}, title={}", userId, req.getTitle());
            PostRes post = boardService.create(userId, req);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            log.error("게시글 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 게시글 수정
     * PUT /api/boards/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostUpdateReq> updatePost(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long postId,
            @RequestBody @Valid PostUpdateReq req) {
        
        Long userId = Long.valueOf(userIdStr);
        
        try {
            log.info("게시글 수정 요청: userId={}, postId={}", userId, postId);
            PostUpdateReq updated = boardService.update(userId, postId, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("게시글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("게시글 수정 권한 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 게시글 삭제
     * DELETE /api/boards/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<PostDeleteRes> deletePost(
            @AuthenticationPrincipal String userIdStr,
            @PathVariable Long postId,
            HttpServletRequest request) {
        
        log.info("게시글 삭제 API 호출: path={}, method={}, userIdStr={}, postId={}, Authorization={}", 
                request.getRequestURI(), request.getMethod(), userIdStr, postId, request.getHeader("Authorization"));

        // 인증 확인
        if (userIdStr == null || userIdStr.isBlank()) {
            log.error("인증된 사용자가 없습니다. SecurityContext: {}", 
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PostDeleteRes.builder()
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(PostDeleteRes.builder()
                    .postId(postId)
                    .userId(null)
                    .message("인증 오류가 발생했습니다.")
                    .build());
        }

        try {
            log.info("게시글 삭제 요청: userId={}, postId={}", userId, postId);
            PostDeleteRes result = boardService.delete(userId, postId);
            log.info("게시글 삭제 성공: userId={}, postId={}", userId, postId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("게시글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(PostDeleteRes.builder()
                    .postId(postId)
                    .userId(userId)
                    .message(e.getMessage())
                    .build());
        } catch (RuntimeException e) {
            log.error("게시글 삭제 권한 오류: userId={}, postId={}, error={}", userId, postId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PostDeleteRes.builder()
                    .postId(postId)
                    .userId(userId)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("게시글 삭제 중 오류 발생: userId={}, postId={}", userId, postId, e);
            return ResponseEntity.internalServerError().body(PostDeleteRes.builder()
                    .postId(postId)
                    .userId(userId)
                    .message("게시글 삭제 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 게시글 목록 조회
     * GET /api/boards?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<BoardListRes>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BoardListRes> posts = boardService.getBoardList(page, size);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("게시글 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 게시글 상세 조회
     * GET /api/boards/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostRes> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal String userIdStr) {
        try {
            // String으로 받은 userId를 Long으로 변환 (로그인하지 않은 경우 null)
            Long userId = null;
            if (userIdStr != null && !userIdStr.isBlank()) {
                try {
                    userId = Long.valueOf(userIdStr);
                } catch (NumberFormatException e) {
                    log.warn("인증된 사용자 ID를 Long으로 변환할 수 없습니다: {}", userIdStr);
                }
            }
            
            PostRes post = boardService.findById(postId, userId);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            log.error("게시글 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("게시글 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
