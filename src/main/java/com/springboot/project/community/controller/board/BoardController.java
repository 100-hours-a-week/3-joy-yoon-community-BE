package com.springboot.project.community.controller.board;

import com.springboot.project.community.dto.board.*;
import com.springboot.project.community.service.board.BoardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *  게시글 관련 컨트롤러
 * - 게시글 작성 / 전체 조회 / 상세 조회
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 작성
     */
    @PostMapping
    public PostRes createPost(
            @RequestParam Long userId,
            @RequestBody @Valid PostCreateReq req) {
        return boardService.create(userId, req);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public PostUpdateReq updatePost(
            @RequestParam Long userId,
            @PathVariable Long postId,
            @RequestBody PostUpdateReq req) {
        return boardService.update(userId, postId, req);
    }

    /**
     * 게시글 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<BoardListRes>> getBoardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BoardListRes> boardList = boardService.getBoardList(page, size);
        return ResponseEntity.ok(boardList);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostRes> findById(@PathVariable Long postId) {
        PostRes postRes = boardService.findById(postId);
        return ResponseEntity.ok(postRes);
    }

}

