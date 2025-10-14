package com.springboot.project.community.repository;

import com.springboot.project.community.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 *  게시글 Repository
 */
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findAllByOrderByCreatedAtDesc();
}
