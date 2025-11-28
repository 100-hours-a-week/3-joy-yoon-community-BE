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
    private String contents;
    private Long likes;
    private Long comments;
    private Long views;
    private LocalDateTime createdAt;

    // Board와 BoardStats를 받아서 DTO 생성
    public static BoardListRes from(Board board, BoardStats stats) {
        return BoardListRes.builder()
                .id(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .author(board.getAuthor().getNickname()) // User의 필드명에 맞게 수정
                .createdAt(board.getCreatedAt())
                .like_count(stats != null ? stats.getLikeCount() : 0L)
                .comment_count(stats != null ? stats.getCommentCount() : 0L)
                .view_count(stats != null ? stats.getViewCount() : 0L)
                .build();
    }
}
