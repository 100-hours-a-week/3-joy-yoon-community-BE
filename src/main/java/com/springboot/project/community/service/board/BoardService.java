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
import org.springframework.transaction.annotation.Isolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BoardStatsRepository boardStatsRepository;
    private final BoardImageRepository boardImageRepository;

    @Transactional
    public PostRes create(Long userId, PostCreateReq req) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Board board = Board.builder()
                .author(author)
                .title(req.getTitle())
                .contents(req.getContents())
                .build();

        boardRepository.save(board);

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

    @Transactional
    public PostUpdateReq update(Long userId, Long postId, PostUpdateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        if (!board.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        board.setTitle(req.getTitle());
        board.setContents(req.getContents());

        boardRepository.save(board);

        return PostUpdateReq.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .createdAt(board.getCreatedAt())
                .build();
    }

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

        // Board의 author 정보를 명시적으로 로딩 (LAZY 로딩 문제 방지)
        List<BoardListRes> responseList = boards.stream()
                .peek(board -> {
                    if (board.getAuthor() != null) {
                        board.getAuthor().getNickname(); // LAZY 로딩 트리거
                        board.getAuthor().getImage(); // 프로필 이미지 로딩
                    }
                })
                .map(board -> BoardListRes.from(board, statsMap.get(board.getPostId())))
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, boards.getTotalElements());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostRes findById(Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 작성자 정보 명시적 로딩 (프로필 이미지 포함)
        if (board.getAuthor() != null) {
            board.getAuthor().getNickname(); // LAZY 로딩 트리거
            board.getAuthor().getUserId(); // LAZY 로딩 트리거
            board.getAuthor().getImage(); // LAZY 로딩 트리거
        }
        
        // Board 엔티티의 모든 필드를 트랜잭션 내에서 미리 로딩 (LAZY 로딩 방지)
        board.getPostId();
        board.getTitle();
        board.getContents();
        board.getCreatedAt();
        board.getUpdatedAt();

        // BoardStats 조회 또는 생성
        BoardStats stats = boardStatsRepository.findById(postId)
                .orElseGet(() -> {
                    BoardStats newStats = BoardStats.builder()
                            .postId(postId)
                            .board(board)  // Board 엔티티 설정 (필수)
                            .likeCount(0L)
                            .commentCount(0L)
                            .viewCount(0L)
                            .build();
                    return boardStatsRepository.save(newStats);
                });

        // 벌크 업데이트로 조회수 증가 (동시성 안전)
        int updated = boardStatsRepository.incrementViewCount(postId);
        
        // 벌크 업데이트 성공 시 메모리의 stats 값도 증가 (응답용)
        if (updated > 0) {
            stats.setViewCount(stats.getViewCount() + 1);
        }

        List<String> imageUrls = boardImageRepository
                .findByBoard_PostIdOrderBySortOrderAsc(postId)
                .stream()
                .map(BoardImage::getImageUrl)
                .toList();

        // 댓글 (작성자 정보 포함하여 로딩)
        List<CommentRes> commentRes = commentRepository
                .findByBoard_PostIdOrderByCreatedAtAsc(postId)
                .stream()
                .peek(comment -> {
                    // 댓글 작성자 정보 명시적 로딩 (프로필 이미지 포함)
                    if (comment.getAuthor() != null) {
                        comment.getAuthor().getImage(); // LAZY 로딩 트리거
                    }
                })
                .map(CommentRes::from)
                .toList();

        return PostRes.of(board, stats, imageUrls, commentRes, false);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostDeleteRes delete(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        if (!board.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 댓글 삭제
        List<Comment> comments = commentRepository.findByBoard_PostIdOrderByCreatedAtAsc(postId);
        if (!comments.isEmpty()) {
            commentRepository.deleteAll(comments);
        }

        // 좋아요 삭제 (실제로는 soft delete이지만 게시글 삭제 시 관련 좋아요도 삭제)
        // BoardLike는 복합키이므로 직접 삭제
        // 실제로는 게시글이 삭제되면 좋아요도 의미가 없으므로 삭제하거나 무시

        // BoardStats 삭제 (cascade로 자동 삭제되지만 명시적으로 삭제)
        boardStatsRepository.findById(postId).ifPresent(boardStatsRepository::delete);

        // BoardImage는 cascade로 자동 삭제됨
        // 게시글 삭제
        boardRepository.delete(board);

        return PostDeleteRes.builder()
                .postId(postId)
                .userId(userId)
                .message("게시글이 삭제되었습니다.")
                .build();
    }
}
