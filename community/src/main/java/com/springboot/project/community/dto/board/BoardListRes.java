package com.springboot.project.community.dto.board;

import com.springboot.project.community.entity.Board;
import com.springboot.project.community.entity.BoardStats;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardListRes {
    private Long id;
    private String title;
    private String author;
    private Long likes;
    private Long comments;
    private Long views;
    private LocalDateTime createdAt;

    // Board와 BoardStats를 받아서 DTO 생성
    public static BoardListRes from(Board board, BoardStats stats) {
        return BoardListRes.builder()
                .id(board.getPostId())
                .title(board.getTitle())
                .author(board.getAuthor().getNickname()) // User의 필드명에 맞게 수정
                .createdAt(board.getCreatedAt())
                .likes(stats != null ? stats.getLikeCount() : 0L)
                .comments(stats != null ? stats.getCommentCount() : 0L)
                .views(stats != null ? stats.getViewCount() : 0L)
                .build();
    }
}
