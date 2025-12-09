package com.springboot.project.community.service.like;

import com.springboot.project.community.dto.like.LikeToggleRes;
import com.springboot.project.community.entity.Board;
import com.springboot.project.community.entity.BoardLike;
import com.springboot.project.community.entity.BoardLikeId;
import com.springboot.project.community.entity.BoardStats;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.BoardLikeRepository;
import com.springboot.project.community.repository.BoardRepository;
import com.springboot.project.community.repository.BoardStatsRepository;
import com.springboot.project.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

/**
 * 좋아요 토글 서비스
 * - Builder 필드명/boolean 게터 문제를 피하기 위해 setter 기반으로 안전하게 작성
 */
@Service
@RequiredArgsConstructor
public class LikeService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final BoardStatsRepository boardStatsRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LikeToggleRes toggle(Long userId, Long postId) {
        // 유저/게시글 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 복합키 구성
        BoardLikeId likeId = new BoardLikeId(userId, postId);

        // 기존 데이터 조회
        BoardLike existing = boardLikeRepository.findById(likeId).orElse(null);
        boolean liked;

        if (existing == null) {
            // 새 좋아요 생성
            BoardLike newLike = new BoardLike();
            newLike.setLikeId(likeId);
            newLike.setUser(user);
            newLike.setBoard(board);
            newLike.setDeleted(false);
            boardLikeRepository.save(newLike);
            liked = true;
        } else {
            // 기존 좋아요 상태 확인
            boolean wasDeleted = existing.isDeleted();
            // 토글
            existing.setDeleted(!wasDeleted);
            boardLikeRepository.save(existing);
            liked = !existing.isDeleted();
        }

        // BoardStats 조회 또는 생성
        BoardStats stats = boardStatsRepository.findById(postId)
                .orElseGet(() -> {
                    BoardStats newStats = BoardStats.builder()
                            .postId(postId)
                            .board(board)  // Board 엔티티 설정 (필수)
                            .viewCount(0L)
                            .likeCount(0L)
                            .commentCount(0L)
                            .build();
                    return boardStatsRepository.save(newStats);
                });

        // 벌크 업데이트로 좋아요 수 증가/감소 (동시성 안전)
        int updated;
        if (liked) {
            // 좋아요 증가
            updated = boardStatsRepository.incrementLikeCount(postId);
            if (updated > 0) {
                stats.setLikeCount(stats.getLikeCount() + 1);
            }
        } else {
            // 좋아요 감소
            updated = boardStatsRepository.decrementLikeCount(postId);
            if (updated > 0) {
                stats.setLikeCount(Math.max(stats.getLikeCount() - 1, 0L));
            }
        }

        // 응답 (DTO가 Long-count면 변환)
        return LikeToggleRes.builder()
                .postId(postId)
                .likeCount(stats.getLikeCount())
                .liked(liked)
                .build();
    }
}
