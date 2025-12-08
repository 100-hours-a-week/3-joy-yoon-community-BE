package com.springboot.project.community.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *  사용자 엔티티 (USERS)
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "USERS")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    // Base64 이미지 문자열을 저장하기 때문에 TEXT 타입으로 변경
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image;

    @Column(name = "use_yn", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean useYn;

    // createdAt, updatedAt 삭제 (부모 상속)

    // @CreatedDate
    // @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    // private LocalDateTime createdAt;

    // @LastModifiedDate
    // @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    // private LocalDateTime updatedAt;
}
