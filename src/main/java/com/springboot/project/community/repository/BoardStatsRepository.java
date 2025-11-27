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

//    @Modifying
//    @Query("UPDATE BoardStats bs SET bs.commentCount = bs.commentCount + 1 WHERE bs.postId = :postId")
//    void incrementCommentCount(@Param("postId") Long postId);
//
//    @Modifying
//    @Query("UPDATE BoardStats bs SET bs.commentCount = bs.commentCount - 1 WHERE bs.postId = :postId")
//    void decrementCommentCount(@Param("postId") Long postId);
}
