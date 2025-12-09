package com.springboot.project.community.service.comment;

import com.springboot.project.community.dto.comment.*;
import com.springboot.project.community.entity.*;
import com.springboot.project.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardStatsRepository boardStatsRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommentRes add(Long userId, Long postId, CommentCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));
        
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        Comment comment = Comment.builder()
                .author(user)
                .board(board)
                .contents(req.getContents())
                .build();
        commentRepository.save(comment);

        // BoardStats가 없으면 생성
        if (!boardStatsRepository.findById(board.getPostId()).isPresent()) {
            BoardStats newStats = BoardStats.builder()
                    .postId(board.getPostId())
                    .board(board)  // Board 엔티티 설정 (필수)
                    .likeCount(0L)
                    .commentCount(0L)
                    .viewCount(0L)
                    .build();
            boardStatsRepository.save(newStats);
        }

        // 벌크 업데이트로 댓글 수 증가 (동시성 안전)
        boardStatsRepository.incrementCommentCount(board.getPostId());

        if (comment.getAuthor() != null) {
            comment.getAuthor().getImage();
        }

        return CommentRes.from(comment);
    }

    @Transactional
    public CommentRes update(Long userId, Long postId, Long commentId, CommentUpdateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. commentId=" + commentId));

        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.setContents(req.getContents());
        commentRepository.save(comment);

        if (comment.getAuthor() != null) {
            comment.getAuthor().getImage();
        }

        return CommentRes.from(comment);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommentDeleteRes delete(Long userId, Long postId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. commentId=" + commentId));

        if (!comment.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);

        // 벌크 업데이트로 댓글 수 감소 (동시성 안전, 0 이하로 내려가지 않음)
        boardStatsRepository.decrementCommentCount(postId);

        return CommentDeleteRes.builder()
                .commentId(commentId)
                .postId(postId)
                .userId(userId)
                .message("댓글이 삭제되었습니다.")
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommentRes> findByPost(Long postId) {
        return commentRepository.findByBoard_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .peek(comment -> {
                    if (comment.getAuthor() != null) {
                        comment.getAuthor().getImage();
                    }
                })
                .map(CommentRes::from)
                .toList();
    }
}
