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
    public CommentRes add(Long userId, Long postId, CommentCreateReq req) {
        // 작성자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId=" + userId));

        // 게시글 확인
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 댓글 생성 및 저장
        Comment comment = Comment.builder()
                .author(user)
                .board(board)
                .contents(req.getContents())
                .build();

        commentRepository.save(comment);

        // 댓글 수 증가
        BoardStats stats = boardStatsRepository.findById(board.getPostId())
                .orElseGet(() -> BoardStats.builder()
                        .postId(board.getPostId())
                        .likeCount(0L)
                        .commentCount(0L)
                        .viewCount(0L)
                        .build());
        stats.setCommentCount(stats.getCommentCount() + 1);
        boardStatsRepository.save(stats);

        // 응답 DTO 반환
        return CommentRes.builder()



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
