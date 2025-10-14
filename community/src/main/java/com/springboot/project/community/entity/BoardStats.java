package com.springboot.project.community.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 게시글 통계 테이블 (BOARD_STATS)
 */
@Entity
@Table(name = "BOARD_STATS",
        indexes = {
                @Index(name = "idx_view_count", columnList = "view_count DESC"),
                @Index(name = "idx_like_count", columnList = "like_count DESC")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardStats {

    @Id
    @Column(name = "post_id", columnDefinition = "BIGINT UNSIGNED")
    private Long postId;

    //  명시적으로 Board를 참조 (FK 명시)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id", // BoardStats.post_id
            referencedColumnName = "post_id", // Board.post_id
            foreignKey = @ForeignKey(name = "fk_board_stats_board")
    )
    private Board board;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;
}
