package com.springboot.project.community.controller.comment;

import com.springboot.project.community.dto.comment.CommentCreateReq;
import com.springboot.project.community.dto.comment.CommentRes;
import com.springboot.project.community.service.comment.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  댓글 관련 컨트롤러
 * - 댓글 작성 / 게시글별 댓글 조회
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public CommentRes addComment(
            @AuthenticationPrincipal(expression = "user.userId") Long userId,
            @RequestBody CommentCreateReq req) {
        return commentService.add(userId, req);
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     */
    @GetMapping("/{postId}")
    public List<CommentRes> getComments(@PathVariable Long postId) {
        return commentService.findByPost(postId);
    }
}

