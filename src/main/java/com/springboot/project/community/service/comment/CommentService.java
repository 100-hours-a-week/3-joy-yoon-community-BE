package com.springboot.project.community.service.comment;

import com.springboot.project.community.dto.comment.*;
import com.springboot.project.community.entity.*;
import com.springboot.project.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 댓글 서비스 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardStatsRepository boardStatsRepository;

    @Transactional
    public CommentRes create(Long userId, Long postId, CommentCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // boardStats가 없으면 생성 (Native Query로 동시성 문제 해결)
        boardStatsRepository.createIfNotExists(postId);

        Comment comment = Comment.builder()
                .author(user)
                .board(board)
                .contents(req.getContents())
                .build();
        commentRepository.save(comment);

        // 댓글 수 증가 (Native Query로 동시성 문제 해결)
        boardStatsRepository.incrementCommentCount(postId);

        // 응답 DTO 반환
        return CommentRes.from(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentRes> findByPost(Long postId) {
        return commentRepository.findByBoard_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(CommentRes::from)
                .toList();
    }


    /**
     * 댓글 수정
     */
    @Transactional
    public CommentRes update(Long userId, Long postId, Long commentId, CommentUpdateReq req) {
        // 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 게시글 검증
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 댓글 검증
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. commentId=" + commentId));

        // 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getBoard().getPostId().equals(postId)) {
            throw new IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 작성자 검증
        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 수정 내용 반영
        comment.setContents(req.getContents());
        commentRepository.save(comment);

        // DTO로 변환하여 반환
        return CommentRes.from(comment);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public CommentDeleteRes delete(Long userId, Long postId, Long commentId) {
        // 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 게시글 검증
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 댓글 검증
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. commentId=" + commentId));

        // 댓글이 해당 게시글에 속하는지 확인
        if (!comment.getBoard().getPostId().equals(postId)) {
            throw new IllegalArgumentException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 작성자 검증
        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);

        // 댓글 수 감소 (Native Query로 동시성 문제 해결)
        boardStatsRepository.decrementCommentCount(postId);

        // 삭제 응답 반환
        return CommentDeleteRes.builder()
                .commentId(commentId)
                .postId(postId)
                .userId(userId)
                .message("댓글이 삭제되었습니다.")
                .build();
    }
}
