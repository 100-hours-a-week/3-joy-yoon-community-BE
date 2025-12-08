package com.springboot.project.community.controller.comment;

import com.springboot.project.community.dto.board.PostUpdateReq;
import com.springboot.project.community.dto.comment.CommentCreateReq;
import com.springboot.project.community.dto.comment.CommentRes;
import com.springboot.project.community.dto.comment.CommentUpdateReq;
import com.springboot.project.community.service.comment.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 *  댓글 관련 컨트롤러
 * - 댓글 작성 / 게시글별 댓글 조회
 */
@RestController
@RequestMapping("/api/v1/boards/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping("/{postId}")
    public CommentCreateReq addComment(
            @AuthenticationPrincipal(expression = "user.userId") Long userId,
            @RequestParam Long postId,
            @RequestBody CommentCreateReq req) {
        return commentService.add(userId, postId, req);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{postId}/{commentId}")
    public ResponseEntity<CommentUpdateReq> updateComment(
            @RequestParam Long userId, // 로그인 사용자의 ID
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateReq req) {

        CommentUpdateReq updated = commentService.update(userId, postId, commentId, req);
        return ResponseEntity.ok(updated);
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     */
    @GetMapping("/{postId}")
    public List<CommentRes> getComments(@PathVariable Long postId) {
        return commentService.findByPost(postId);
    }
}

