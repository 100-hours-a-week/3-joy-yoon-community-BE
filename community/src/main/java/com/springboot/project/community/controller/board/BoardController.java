package com.springboot.project.community.controller.board;

import com.springboot.project.community.dto.board.PostCreateReq;
import com.springboot.project.community.dto.board.PostRes;
import com.springboot.project.community.entity.Board;
import com.springboot.project.community.service.board.BoardService;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

//    /**
//     * 게시글 수정
//     */
//    @PutMapping
//    public PostUpdateRes createPost(
//            @RequestParam Long userId,
//            @RequestBody @Valid PostCreateReq req) {
//        return boardService.create(userId, req);
//    }

    /**
     * 게시글 목록 조회
     */
    @GetMapping
    public List<Board> getAllPosts() {
        return boardService.findAll();
    }
}

