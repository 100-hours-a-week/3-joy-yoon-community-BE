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
            // 새 좋아요 (빌더 대신 setter로 안전 생성)
            BoardLike newLike = new BoardLike();
            newLike.setLikeId(likeId);
            newLike.setUser(user);
            newLike.setBoard(board);
            newLike.setDeleted(false);
            boardLikeRepository.save(newLike);
            liked = true;
        } else {
            // 토글 (boolean 게터는 isDeleted())
            existing.setDeleted(!existing.isDeleted());
            boardLikeRepository.save(existing);
            liked = !existing.isDeleted();
        }

        // 통계 갱신 (BoardStats의 count가 INT라면 Integer로 취급)
        BoardStats stats = boardStatsRepository.findById(postId)
                .orElse(BoardStats.builder()
                        .postId(postId)
                        .viewCount(0L) // INT면 Integer 0
                        .likeCount(0L)
                        .commentCount(0L)
                        .build());

        Long newLikeCount = stats.getLikeCount() + (liked ? 1 : -1);
        stats.setLikeCount(Math.max(newLikeCount, 0L));
        boardStatsRepository.save(stats);

        // 응답 (DTO가 Long-count면 변환)
        return LikeToggleRes.builder()
                .postId(postId)
                .likeCount((long) stats.getLikeCount()) // Integer → Long 변환
                .liked(liked)
                .build();
    }
}
