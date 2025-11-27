package com.springboot.project.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시판 엔티티 (BOARD)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "BOARD",
        indexes = {
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_created", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", columnDefinition = "BIGINT UNSIGNED")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_board_user")
    )
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String contents;

    @CreatedDate
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BoardImage> images = new ArrayList<>();

    // 연관관계 편의 메서드 - 개별 추가
    public void addImage(BoardImage image) {
        images.add(image);
        image.setBoard(this);
    }

    // 연관관계 편의 메서드 - 전체 교체
    public void setImages(List<BoardImage> newImages) {
        images.clear();
        if (newImages != null) {
            for (BoardImage image : newImages) {
                addImage(image);
            }
        }
    }
}

//package com.springboot.project.community.entity;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.springboot.project.community.dto.board.BoardListRes;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.springframework.data.annotation.CreatedDate;
//import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
///**
// *  게시판 엔티티 (BOARD)
// */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
//@Table(
//        name = "BOARD",
//        indexes = {
//                @Index(name = "idx_user", columnList = "user_id"),
//                @Index(name = "idx_created", columnList = "created_at")
//        }
//)
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Board {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "post_id", columnDefinition = "BIGINT UNSIGNED")
//    private Long postId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "user_id",
//            nullable = false,
//            foreignKey = @ForeignKey(name = "fk_board_user")
//    )
//    private User author;
//
//    @Column(nullable = false, length = 200)
//    private String title;
//
//    @Lob
//    @Column(nullable = false)
//    private String contents;
//
//    @CreatedDate
//    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
//    private LocalDateTime createdAt;
//
//    @LastModifiedDate
//    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
//    private LocalDateTime updatedAt;
//
//    // 이미지 리스트 Getter
//    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<BoardImage> images = new ArrayList<>();
//
//}
