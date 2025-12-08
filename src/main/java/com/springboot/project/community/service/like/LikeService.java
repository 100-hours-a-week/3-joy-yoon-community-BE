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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Transactional
    public LikeToggleRes toggle(Long userId, Long postId) {
        // 1) 유저/게시글 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2) boardStats가 없으면 생성 (Native Query로 동시성 문제 해결)
        boardStatsRepository.createIfNotExists(postId);

        // 3) 복합키 구성 (본인의 userId로만 조회)
        BoardLikeId likeId = new BoardLikeId(userId, postId);

        // 4) 본인이 누른 좋아요만 확인 (남이 누른 좋아요는 조회되지 않음)
        BoardLike existing = boardLikeRepository.findById(likeId).orElse(null);
        boolean liked;

        if (existing == null) {
            // 본인이 좋아요를 누르지 않았으면 → 좋아요 추가
            BoardLike newLike = new BoardLike();
            newLike.setLikeId(likeId);
            newLike.setUser(user);
            newLike.setBoard(board);
            newLike.setDeleted(false);
            boardLikeRepository.save(newLike);
            liked = true;
            
            // 좋아요 수 증가 (Native Query로 동시성 문제 해결)
            boardStatsRepository.incrementLikeCount(postId);
        } else if (!existing.isDeleted()) {
            // 본인이 누른 좋아요면 → 좋아요 취소
            existing.setDeleted(true);
            boardLikeRepository.save(existing);
            liked = false;
            
            // 좋아요 수 감소 (Native Query로 동시성 문제 해결)
            boardStatsRepository.decrementLikeCount(postId);
        } else {
            // 이미 취소된 좋아요면 → 다시 좋아요
            existing.setDeleted(false);
            boardLikeRepository.save(existing);
            liked = true;
            
            // 좋아요 수 증가 (Native Query로 동시성 문제 해결)
            boardStatsRepository.incrementLikeCount(postId);
        }

        // 5) 현재 좋아요 수 조회
        BoardStats stats = boardStatsRepository.findById(postId)
                .orElseGet(() -> BoardStats.builder()
                        .postId(postId)
                        .likeCount(0L)
                        .commentCount(0L)
                        .viewCount(0L)
                        .build());

        // 6) 응답
        return LikeToggleRes.builder()
                .postId(postId)
                .likeCount(stats.getLikeCount())
                .liked(liked)
                .build();
    }
}
