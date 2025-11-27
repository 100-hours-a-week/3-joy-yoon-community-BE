package com.springboot.project.community.service.board;

import com.springboot.project.community.dto.board.PostCreateReq;
import com.springboot.project.community.dto.board.PostRes;
import com.springboot.project.community.dto.board.PostUpdateReq;
import com.springboot.project.community.entity.Board;
import com.springboot.project.community.entity.BoardStats;
import com.springboot.project.community.entity.User;
import com.springboot.project.community.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito 환경 사용
class BoardServiceTest {

    @InjectMocks // 가짜 객체들을 주입받을 대상
    private BoardService boardService;

    // BoardService가 의존하는 Repository들을 Mock(가짜)으로 선언
    @Mock private BoardRepository boardRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private BoardStatsRepository boardStatsRepository;
    @Mock private BoardImageRepository boardImageRepository;

    @Test
    @DisplayName("게시글 생성 성공")
    void create_Success() {
        // 1. Given (준비)
        Long userId = 1L;
        PostCreateReq req = PostCreateReq.builder()
                .title("테스트 제목")
                .contents("테스트 내용")
                .imageUrls(List.of("image1.jpg", "image2.jpg"))
                .build();

        User user = User.builder().userId(userId).nickname("작성자").build();
        Board board = Board.builder()
                .postId(100L) // 저장 후 생성될 ID 가정
                .author(user)
                .title(req.getTitle())
                .contents(req.getContents())
                .build();

        // userRepository가 호출되면 위의 user를 반환하도록 설정
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // boardRepository.save가 호출되면 위의 board를 반환하도록 설정 (실제 저장은 안됨)
        given(boardRepository.save(any(Board.class))).willReturn(board);

        // 2. When (실행)
        PostRes result = boardService.create(userId, req);

        // 3. Then (검증)
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        assertThat(result.getAuthor()).isEqualTo("작성자");
        assertThat(result.getImageUrls()).hasSize(2);
        
        // saveAll이 호출되었는지 확인 (이미지가 있으므로)
        verify(boardImageRepository).saveAll(any());
    }

    @Test
    @DisplayName("게시글 수정 성공 - 본인 작성글")
    void update_Success() {
        // 1. Given
        Long userId = 1L;
        Long postId = 100L;
        
        // 수정 요청 데이터
        PostUpdateReq req = PostUpdateReq.builder()
                .title("수정된 제목")
                .contents("수정된 내용")
                .build();

        User user = User.builder().userId(userId).build();
        Board existingBoard = Board.builder()
                .postId(postId)
                .author(user) // 작성자 일치
                .title("원래 제목")
                .contents("원래 내용")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(boardRepository.findById(postId)).willReturn(Optional.of(existingBoard));

        // 2. When
        PostUpdateReq result = boardService.update(userId, postId, req);

        // 3. Then
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getContents()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아님")
    void update_Fail_NotAuthor() {
        // 1. Given
        Long ownerId = 1L;
        Long intruderId = 2L; // 다른 사람
        Long postId = 100L;

        PostUpdateReq req = PostUpdateReq.builder().title("수정 시도").build();

        User intruder = User.builder().userId(intruderId).build();
        User owner = User.builder().userId(ownerId).build();
        
        Board existingBoard = Board.builder()
                .postId(postId)
                .author(owner) // 작성자는 owner
                .build();

        given(userRepository.findById(intruderId)).willReturn(Optional.of(intruder));
        given(boardRepository.findById(postId)).willReturn(Optional.of(existingBoard));

        // 2. When & Then (예외 발생 검증)
        assertThatThrownBy(() -> boardService.update(intruderId, postId, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("본인이 작성한 게시글만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("게시글 상세 조회 성공 (조회수 증가 포함)")
    void findById_Success() {
        // 1. Given
        Long postId = 100L;
        Board board = Board.builder()
                .postId(postId)
                .title("제목")
                .author(User.builder().nickname("작성자").build())
                .build();
        
        BoardStats stats = BoardStats.builder().postId(postId).viewCount(10L).build();

        given(boardRepository.findById(postId)).willReturn(Optional.of(board));
        given(boardStatsRepository.findById(postId)).willReturn(Optional.of(stats));
        // 이미지, 댓글 Repository는 빈 리스트 반환 처리 (NullPointerException 방지)
        given(boardImageRepository.findByBoard_PostIdOrderBySortOrderAsc(postId)).willReturn(List.of());
        given(commentRepository.findByBoard_PostIdOrderByCreatedAtAsc(postId)).willReturn(List.of());

        // 2. When
        PostRes result = boardService.findById(postId);

        // 3. Then
        assertThat(result.getTitle()).isEqualTo("제목");
        
        // 조회수가 10 -> 11로 증가해서 저장되었는지 확인
        assertThat(stats.getViewCount()).isEqualTo(11L);
        verify(boardStatsRepository).save(stats);
    }
}