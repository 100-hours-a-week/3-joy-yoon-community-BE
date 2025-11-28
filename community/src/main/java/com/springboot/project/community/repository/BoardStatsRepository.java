package com.springboot.project.community.repository;

import com.springboot.project.community.entity.BoardStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardStatsRepository extends JpaRepository<BoardStats, Long> {

    // 댓글 수 1 증가 (벌크 업데이트. DB에서 직접 값 증가 → 동시성에 안전)
    // clearAutomatically: 벌크 연산 후 영속성 컨텍스트 초기화
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardStats bs SET bs.commentCount = bs.commentCount + 1 WHERE bs.postId = :postId")
    int incrementCommentCount(@Param("postId") Long postId);

    // 댓글 수 1 감소 (0 아래로 내려가지 않도록 조건 추가)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardStats bs SET bs.commentCount = bs.commentCount - 1 WHERE bs.postId = :postId AND bs.commentCount > 0")
    int decrementCommentCount(@Param("postId") Long postId);

    // 좋아요 수 1 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardStats bs SET bs.likeCount = bs.likeCount + 1 WHERE bs.postId = :postId")
    int incrementLikeCount(@Param("postId") Long postId);

    // 좋아요 수 1 감소 (0 아래로 내려가지 않도록 조건 추가)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE BoardStats bs SET bs.likeCount = bs.likeCount - 1 WHERE bs.postId = :postId AND bs.likeCount > 0")
    int decrementLikeCount(@Param("postId") Long postId);
}
