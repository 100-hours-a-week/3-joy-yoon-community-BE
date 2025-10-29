package com.springboot.project.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게시글 이미지 (BOARD_IMAGE)
 * - QueryDSL에서 사용할 수 있도록 JPA 매핑 구조를 유지
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "BOARD_IMAGE",
        indexes = {
                @Index(name = "idx_post", columnList = "post_id, sort_order")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", columnDefinition = "BIGINT UNSIGNED")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_board_image")
    )
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_board_image_user"))
    private User user;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    @CreatedDate
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}


//package com.springboot.project.community.entity;
//
//import java.time.LocalDateTime;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
///**
// *  게시글 이미지 (BOARD_IMAGE)
// */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
//@Table(
//        name = "BOARD_IMAGE",
//        indexes = {
//                @Index(name = "idx_post", columnList = "post_id, sort_order")
//        }
//)
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class BoardImage {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "image_id", columnDefinition = "INT UNSIGNED")
//    private Integer imageId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "post_id",
//            nullable = false,
//            foreignKey = @ForeignKey(name = "fk_board_image")
//    )
//    private Board board;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "user_id",
//            foreignKey = @ForeignKey(name = "fk_board_image_user")
//    )
//    private User user;
//
//    @Column(name = "image_url", nullable = false, length = 255)
//    private String imageUrl;
//
//    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
//    private Integer sortOrder;
//
//    @CreatedDate
//    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
//    private LocalDateTime createdAt;
//}
