package com.springboot.project.community.service.board;

import com.springboot.project.community.dto.board.*;
import com.springboot.project.community.entity.*;
import com.springboot.project.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** 게시글 서비스 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardStatsRepository boardStatsRepository;

    @Transactional
    public PostRes create(Long userId, PostCreateReq req) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 없음"));

        Board board = Board.builder()
                .author(author)
                .title(req.getTitle())       // ✅ 수정
                .contents(req.getContents()) // ✅ 수정
                .build();

        boardRepository.save(board);

        boardStatsRepository.save(BoardStats.builder()
                .postId(board.getPostId())
                .viewCount(0L).likeCount(0L).commentCount(0L).build());

        return PostRes.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .author(author.getNickname())
                .createdAt(board.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Board> findAll() {
        return boardRepository.findAll();
    }
}

