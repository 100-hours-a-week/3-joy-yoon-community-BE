package com.springboot.project.community.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 좋아요 (BOARD_LIKE)
 * - PK: (user_id, post_id) 복합키
 * - 삭제 대신 is_deleted 토글
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "BOARD_LIKE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardLike {

    /** 복합키 (user_id + post_id) */
    @EmbeddedId
    private BoardLikeId likeId;

    /** USER 매핑 (likeId.userId 사용) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** BOARD 매핑 (likeId.postId 사용) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Board board;

    /** 좋아요 상태 (삭제 대신 토글) */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;  // Lombok이 isDeleted() 게터 생성

    /** 수정시각 (DB default/on update를 그대로 사용) */
    @LastModifiedDate
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
