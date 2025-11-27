package com.springboot.project.community.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *  이미지 파일 관리 테이블 (IMAGE_FILE)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
