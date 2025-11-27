package com.springboot.project.community.repository;

import com.springboot.project.community.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  게시글 Repository
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
//    List<Board> findAllByOrderByCreatedAtDesc();
    @Query("SELECT b FROM Board b ORDER BY b.id DESC")
    List<Board> findAllDesc();
}
