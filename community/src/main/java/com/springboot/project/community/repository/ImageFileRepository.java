package com.springboot.project.community.repository;

import com.springboot.project.community.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로필 이미지 파일 Repository
 */
@Repository
public interface ImageFileRepository extends JpaRepository<ImageFile, Integer> {
    Optional<ImageFile> findByUrl(String url);
    
    @Modifying
    @Query("UPDATE ImageFile i SET i.refCount = i.refCount + 1 WHERE i.imageId = :imageId")
    void incrementRefCount(@Param("imageId") Integer imageId);
    
    // User 관계가 있는 경우에만 사용 가능
    // List<ImageFile> findByUser_UserId(Long userId);
    // List<ImageFile> findByUser_UserIdOrderByCreatedAtAsc(Long userId);
    // long countByUser_UserId(Long userId);
}

