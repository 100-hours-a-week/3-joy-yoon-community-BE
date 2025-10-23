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
    public CommentRes add(Long userId, CommentCreateReq req) {
        User user = userRepository.findById(userId).orElseThrow();
        Board board = boardRepository.findById(req.postId()).orElseThrow();

        Comment comment = Comment.builder()
                .author(user).board(board)
                .contents(req.contents())
                .build();
        commentRepository.save(comment);

        BoardStats stats = boardStatsRepository.findById(board.getPostId()).orElseThrow();
        stats.setCommentCount(stats.getCommentCount() + 1);

        return CommentRes.builder()
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
                        .contents(c.getContents())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }
}
