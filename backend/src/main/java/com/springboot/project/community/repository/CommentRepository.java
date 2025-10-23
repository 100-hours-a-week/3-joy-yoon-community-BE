package com.springboot.project.community.repository;

import com.springboot.project.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 *  댓글 Repository
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoard_PostIdOrderByCreatedAtAsc(Long postId);
}
