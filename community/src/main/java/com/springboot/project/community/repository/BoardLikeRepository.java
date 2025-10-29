package com.springboot.project.community.repository;

import com.springboot.project.community.entity.BoardLike;
import com.springboot.project.community.entity.BoardLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *  좋아요 Repository
 */
@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, BoardLikeId> {
    long countByBoard_PostIdAndDeletedFalse(Long postId);
    boolean existsByLikeIdAndDeletedFalse(BoardLikeId likeId);
}
