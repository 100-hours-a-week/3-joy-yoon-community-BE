package com.springboot.project.community.service.board;

import com.springboot.project.community.dto.board.*;
import com.springboot.project.community.dto.comment.CommentRes;
import com.springboot.project.community.entity.*;
import com.springboot.project.community.repository.*;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 
 * 게시글 서비스 
*/
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
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

        // BOARD_STATS 초기화
        BoardStats stats = BoardStats.builder()
                .board(board) // @MapsId로 인해 board의 ID를 따라감
                .viewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .build();
        boardStatsRepository.save(stats);

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
    public PostUpdateReq update(Long userId, Long postId, PostUpdateReq req) {
        // 요청한 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 게시글 존재 확인
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 작성자 검증
        if (!board.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 게시글만 수정할 수 있습니다.");
            // 또는 Spring 표준 예외로:
            // throw new AccessDeniedException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 수정 내용 반영
        board.setTitle(req.getTitle());
        board.setContents(req.getContents());

        // 저장 (JPA 영속성 컨텍스트에 의해 자동 update)
        boardRepository.save(board);

        // DTO 변환 후 반환
        return PostUpdateReq.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .createdAt(board.getCreatedAt())
                .build();
    }

    /**
     * 전체 게시글 조회
     */
    @Transactional(readOnly = true)
    public Page<BoardListRes> getBoardList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Board> boards = boardRepository.findAll(pageable);

        List<Long> postIds = boards.stream()
                .map(Board::getPostId)
                .collect(Collectors.toList());

        List<BoardStats> statsList = boardStatsRepository.findByPostIdIn(postIds);
        Map<Long, BoardStats> statsMap = statsList.stream()
                .collect(Collectors.toMap(BoardStats::getPostId, stats -> stats));

        List<BoardListRes> responseList = boards.stream()
                .map(board -> BoardListRes.from(board, statsMap.get(board.getPostId())))
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, boards.getTotalElements());
    }

    /**
     * 게시글 상세 조회
     */
    @Transactional
    public PostRes findById(Long postId) {
        // 게시글
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 통계
        BoardStats stats = boardStatsRepository.findById(postId)
                .orElseGet(() -> BoardStats.builder()
                        .postId(postId)
                        .likeCount(0L)
                        .commentCount(0L)
                        .viewCount(0L)
                        .build());

        // 조회수 증가
        stats.setViewCount(stats.getViewCount() + 1);
        boardStatsRepository.save(stats);

        // 이미지 (정렬 포함)
        List<String> imageUrls = boardImageRepository
                .findByBoard_PostIdOrderBySortOrderAsc(postId)
                .stream()
                .map(BoardImage::getImageUrl)
                .toList();

        // 댓글
        List<CommentRes> commentRes = commentRepository
                .findByBoard_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .map(CommentRes::from)
                .toList();

        return PostRes.of(board, stats, commentRes);
    }
}

