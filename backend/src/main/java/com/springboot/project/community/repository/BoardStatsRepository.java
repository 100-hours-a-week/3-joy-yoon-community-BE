package com.springboot.project.community.repository;

import com.springboot.project.community.entity.BoardStats;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *  게시글 통계 Repository
 */
public interface BoardStatsRepository extends JpaRepository<BoardStats, Long> {
}
