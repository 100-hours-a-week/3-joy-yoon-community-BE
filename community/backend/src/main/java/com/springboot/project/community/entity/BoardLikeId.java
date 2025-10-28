package com.springboot.project.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BOARD_LIKE의 복합키 (user_id + post_id)
 * - 실제 테이블 컬럼은 user_id, post_id 두 개이며
 * - JPA에서는 이를 하나의 키 객체로 묶어 사용한다.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardLikeId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;   // DB: INT UNSIGNED ↔ JPA: Long (호환 OK)

    @Column(name = "post_id", nullable = false)
    private Long postId;   // DB: BIGINT UNSIGNED ↔ JPA: Long
}
