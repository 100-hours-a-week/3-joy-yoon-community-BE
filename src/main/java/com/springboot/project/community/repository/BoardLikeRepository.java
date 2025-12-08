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
    void deleteByBoard_PostId(Long postId);
    
    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인 (삭제되지 않은 것만)
     */
    boolean existsByLikeId_UserIdAndLikeId_PostIdAndDeletedFalse(Long userId, Long postId);
}
