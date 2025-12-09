package com.springboot.project.community.controller.comment;

import com.springboot.project.community.dto.comment.CommentCreateReq;
import com.springboot.project.community.dto.comment.CommentRes;
import com.springboot.project.community.dto.comment.CommentDeleteRes;
import com.springboot.project.community.dto.comment.CommentUpdateReq;
import com.springboot.project.community.service.comment.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentRes> addComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateReq req) {
        CommentRes comment = commentService.add(userId, postId, req);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentRes> updateComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateReq req) {
        CommentRes updated = commentService.update(userId, postId, commentId, req);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDeleteRes> deleteComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        CommentDeleteRes result = commentService.delete(userId, postId, commentId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentRes>> getComments(@PathVariable Long postId) {
        List<CommentRes> comments = commentService.findByPost(postId);
        return ResponseEntity.ok(comments);
    }
}
