package com.springboot.project.community.service.board;

import com.springboot.project.community.dto.board.*;
import com.springboot.project.community.entity.*;
import com.springboot.project.community.repository.*;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** 게시글 서비스 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardStatsRepository boardStatsRepository;
    private final BoardImageRepository boardImageRepository;

    /**
     * 게시글 생성
     */
    @Transactional
    public PostRes create(Long userId, PostCreateReq req) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 게시글 저장
        Board board = Board.builder()
                .author(author)
                .title(req.getTitle())
                .contents(req.getContents())
                .build();

        boardRepository.save(board);

        // 이미지 저장
        List<BoardImage> images = new ArrayList<>();
        if (req.getImageUrls() != null) {
            int order = 0;
            for (String url : req.getImageUrls()) {
                BoardImage image = BoardImage.builder()
                        .board(board)
                        .user(author)
                        .imageUrl(url)
                        .sortOrder(order++)
                        .build();
                images.add(image);
            }
            boardImageRepository.saveAll(images);
        }

        // 응담 DTO
        List<String> imageUrls = req.getImageUrls() != null ? req.getImageUrls() : List.of();

        return PostRes.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .imageUrls(imageUrls)
                .author(author.getNickname())
                .createdAt(board.getCreatedAt())
                .build();
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public PostRes update(Long userId, Long postId, PostUpdateReq req) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        // 게시글 내용 수정
        if (!board.getAuthor().getUserId().equals(userId)) {
            throw new IllegalStateException("작성자만 수정이 가능합니다.");
        }

        // 제목 수정
        if (req.getTitle() != null && !req.getTitle().isBlank()){
            board.setTitle(req.getTitle());
        } else if (req.getContents() != null && !req.getContents().isBlank()) {
            board.setContents(req.getContents());
        } else if (req.getImageUrls() != null) {
            // 전체 교체
            boardImageRepository.deleteAllByBoard_PostId(postId);

            List<BoardImage> newImages = new ArrayList<>();
            int order = 0;
            for (String url : req.getImageUrls()) {
                BoardImage image = BoardImage.builder()
                        .board(board)
                        .user(board.getAuthor())
                        .imageUrl(url)
                        .sortOrder(order++)
                        .build();
                newImages.add(image);
            }
            boardImageRepository.saveAll(newImages);
        }


        boardRepository.save(board);

        return PostRes.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .author(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();

    }

    /**
     * 전체 게시글 조회
     */
    @Transactional
    public List<PostRes> findAll() {
        List<Board> boards = boardRepository.findAllByOrderByCreatedAtDesc();
        return boards.stream()
                .map(board -> PostRes.builder()
                        .postId(board.getPostId())
                        .title(board.getTitle())
                        .contents(board.getContents())
                        .author(board.getAuthor().getNickname())
                        .createdAt(board.getCreatedAt())
                        .updatedAt(board.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

}

