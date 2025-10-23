package com.springboot.project.community.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  이미지 파일 관리 테이블 (IMAGE_FILE)
 */
@Entity
@Table(name = "IMAGE_FILE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", columnDefinition = "INT UNSIGNED")
    private Integer imageId;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(name = "ref_count", columnDefinition = "INT UNSIGNED DEFAULT 1")
    private Integer refCount;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
