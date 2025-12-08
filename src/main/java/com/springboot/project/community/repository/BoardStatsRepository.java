package com.springboot.project.community.repository;

import com.springboot.project.community.entity.BoardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  게시글 통계 Repository
 */
@Repository
public interface BoardStatsRepository extends JpaRepository<BoardStats, Long> {
    /**
     * 여러 게시글 ID로 통계 정보 조회
     */
    List<BoardStats> findByPostIdIn(List<Long> postIds);

    /**
     * BoardStats가 없으면 생성 (Native Query로 동시성 문제 해결)
     */
    @Modifying
    @Query(value = "INSERT IGNORE INTO BOARD_STATS (post_id, view_count, like_count, comment_count) VALUES (:postId, 0, 0, 0)", nativeQuery = true)
    void createIfNotExists(@Param("postId") Long postId);

    /**
     * 댓글 수 증가 (Native Query로 동시성 문제 해결)
     */
    @Modifying
    @Query(value = "UPDATE BOARD_STATS SET comment_count = comment_count + 1 WHERE post_id = :postId", nativeQuery = true)
    void incrementCommentCount(@Param("postId") Long postId);

    /**
     * 댓글 수 감소 (Native Query로 동시성 문제 해결)
     */
    @Modifying
    @Query(value = "UPDATE BOARD_STATS SET comment_count = GREATEST(comment_count - 1, 0) WHERE post_id = :postId", nativeQuery = true)
    void decrementCommentCount(@Param("postId") Long postId);

    /**
     * 좋아요 수 증가 (Native Query로 동시성 문제 해결)
     */
    @Modifying
    @Query(value = "UPDATE BOARD_STATS SET like_count = like_count + 1 WHERE post_id = :postId", nativeQuery = true)
    void incrementLikeCount(@Param("postId") Long postId);

    /**
     * 좋아요 수 감소 (Native Query로 동시성 문제 해결)
     */
    @Modifying
    @Query(value = "UPDATE BOARD_STATS SET like_count = GREATEST(like_count - 1, 0) WHERE post_id = :postId", nativeQuery = true)
    void decrementLikeCount(@Param("postId") Long postId);

    /**
     * 조회수 증가 (Native Query로 동시성 문제 해결)
     */
    @Modifying
    @Query(value = "UPDATE BOARD_STATS SET view_count = view_count + 1 WHERE post_id = :postId", nativeQuery = true)
    void incrementViewCount(@Param("postId") Long postId);
}
