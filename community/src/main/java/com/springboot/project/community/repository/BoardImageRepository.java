package com.springboot.project.community.repository;

import com.springboot.project.community.entity.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *  게시글 이미지 Repository
 */
@Repository
public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {
//    void deleteAllByBoard_PostId(Long postId);
    List<BoardImage> findByBoard_PostIdOrderBySortOrderAsc(Long postId);
}
