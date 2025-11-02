package com.springboot.project.community.service.comment;

import com.springboot.project.community.dto.board.PostUpdateReq;
import com.springboot.project.community.dto.comment.*;
import com.springboot.project.community.entity.*;
import com.springboot.project.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    public CommentCreateReq add(Long userId, Long postId, CommentCreateReq req) {
        User user = userRepository.findById(userId).orElseThrow();
        Board board = boardRepository.findById(req.getPostId()).orElseThrow();

        Comment comment = Comment.builder()
                .author(user)
                .board(board)
                .contents(req.getContents())
                .build();
        commentRepository.save(comment);

        BoardStats stats = boardStatsRepository.findById(board.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글 통계 정보가 없습니다."));
        stats.setCommentCount(stats.getCommentCount() + 1);
        boardStatsRepository.save(stats); // 명시적으로 저장

        // 응답 DTO 반환
        return CommentCreateReq.builder()
                .commentId(comment.getCommentId())
                .postId(board.getPostId())
                .author(user.getNickname())
                .contents(comment.getContents())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommentRes> findByPost(Long postId) {
        return commentRepository.findByBoard_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(c -> CommentRes.builder()
                        .commentId(c.getCommentId())
                        .postId(postId)
                        .author(c.getAuthor() != null ? c.getAuthor().getNickname() : "(탈퇴회원)")
                        .content(c.getContents())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }


    /**
     * 게시글 수정
     */
    @Transactional
    public CommentUpdateReq update(Long userId, Long postId, Long commentId, CommentUpdateReq req) {

        // 사용자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 게시글 검증
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 댓글 검증
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. commentId=" + commentId));

        // 작성자 검증
        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 수정 내용 반영
        comment.setContents(req.getContents());
        comment.setUpdatedAt(LocalDateTime.now());

        // 저장 (JPA 자동 update)
        commentRepository.save(comment);

        // DTO로 변환하여 반환
        return CommentUpdateReq.from(comment);
    }
}
