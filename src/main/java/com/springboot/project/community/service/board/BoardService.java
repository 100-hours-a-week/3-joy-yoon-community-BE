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

/** 게시글 서비스 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final BoardLikeRepository boardLikeRepository;
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

        Board savedBoard = boardRepository.save(board);

        // BoardStats 초기화 (모든 카운트를 0으로)
        boardStatsRepository.createIfNotExists(savedBoard.getPostId());

        // 이미지 저장 (board_image 테이블에 별도 관리)
        List<BoardImage> images = new ArrayList<>();
        List<String> imageUrlList = new ArrayList<>();
        
        // 단수 image 필드 처리
        if (req.getImage() != null && !req.getImage().isBlank()) {
            imageUrlList.add(req.getImage());
        }
        
        // 복수 imageUrls 필드 처리
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            imageUrlList.addAll(req.getImageUrls());
        }
        
        // board_image 테이블에 저장
        if (!imageUrlList.isEmpty()) {
            int order = 0;
            for (String url : imageUrlList) {
                BoardImage boardImage = BoardImage.builder()
                        .board(savedBoard)
                        .user(author)
                        .imageUrl(url)
                        .sortOrder(order++)
                        .build();
                images.add(boardImage);
            }
            boardImageRepository.saveAll(images);
            savedBoard.setImages(images); // 연관관계 설정
        }

        // BoardStats 조회
        BoardStats stats = boardStatsRepository.findById(savedBoard.getPostId())
                .orElseGet(() -> BoardStats.builder()
                        .postId(savedBoard.getPostId())
                        .likeCount(0L)
                        .commentCount(0L)
                        .viewCount(0L)
                        .build());

        // PostRes.of()에서 comments.size()를 사용하여 실제 댓글 수 표시
        return PostRes.of(savedBoard, stats, new ArrayList<>(), false); // 새 게시글은 좋아요 없음
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

        // 이미지 처리 (기존 이미지 삭제 후 새로 추가)
        // 기존 이미지 삭제
        boardImageRepository.deleteByBoard_PostId(postId);
        
        // 새 이미지 추가 (BOARD_IMAGE만 사용 - 게시글 이미지)
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            List<BoardImage> newImages = new ArrayList<>();
            int order = 0;
            for (String url : req.getImageUrls()) {
                // BOARD_IMAGE 테이블에 저장
                BoardImage image = BoardImage.builder()
                        .board(board)
                        .user(user) // 게시글 작성자 ID 저장
                        .imageUrl(url)
                        .sortOrder(order++)
                        .build();
                newImages.add(image);
            }
            boardImageRepository.saveAll(newImages);
            board.setImages(newImages); // 연관관계 설정
        } else {
            board.setImages(new ArrayList<>()); // 이미지가 없으면 빈 리스트로 설정
        }

        // JPA가 변경 감지하여 업데이트
        return PostUpdateReq.builder()
                .postId(board.getPostId())
                .title(board.getTitle())
                .contents(board.getContents())
                .imageUrls(req.getImageUrls())
                .author(board.getAuthor().getNickname())
                .userId(board.getAuthor().getUserId())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    /**
     * 게시글 삭제
     * 외래키 제약 조건을 피하기 위해 연관된 데이터를 먼저 삭제한 후 게시글을 삭제합니다.
     */
    @Transactional
    public PostDeleteRes delete(Long userId, Long postId) {
        // 게시글 존재 확인
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // 작성자 검증
        if (!board.getAuthor().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 외래키 제약 조건을 피하기 위해 연관된 데이터를 먼저 삭제
        // 1. 댓글 삭제
        commentRepository.deleteByBoard_PostId(postId);
        
        // 2. 좋아요 삭제
        boardLikeRepository.deleteByBoard_PostId(postId);
        
        // 3. 이미지 삭제 (board_image 테이블)
        boardImageRepository.deleteByBoard_PostId(postId);
        
        // 4. 통계 삭제 (board_stats 테이블)
        boardStatsRepository.deleteById(postId);
        
        // 5. 게시글 삭제 (board 테이블)
        boardRepository.delete(board);
        
        // 삭제 응답 반환
        return PostDeleteRes.builder()
                .postId(postId)
                .userId(userId)
                .message("게시글이 삭제되었습니다.")
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
     * 상세 게시글 조회
     */
    @Transactional
    public PostRes findById(Long postId, Long userId) {
        // 게시글
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        // boardStats가 없으면 생성 (Native Query로 동시성 문제 해결)
        boardStatsRepository.createIfNotExists(postId);

        // 조회수 증가 (Native Query로 동시성 문제 해결)
        boardStatsRepository.incrementViewCount(postId);

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

        // 응답용 통계 조회
        BoardStats stats = boardStatsRepository.findById(postId)
                .orElseGet(() -> BoardStats.builder()
                        .postId(postId)
                        .likeCount(0L)
                        .commentCount(0L)
                        .viewCount(0L)
                        .build());

        // 현재 사용자가 좋아요를 눌렀는지 확인
        Boolean isLiked = false;
        if (userId != null) {
            isLiked = boardLikeRepository.existsByLikeId_UserIdAndLikeId_PostIdAndDeletedFalse(userId, postId);
        }

        // PostRes.of()에서 comments.size()를 사용하여 실제 댓글 수 표시
        return PostRes.of(board, stats, commentRes, isLiked);
    }
}

