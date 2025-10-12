package com.springboot.project.community.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 게시글 작성 및 수정 시 클라이언트에서 전달하는 요청 데이터를 담는 DTO입니다.
 * title과 contents는 필수 입력값이며, imageUrl은 선택사항입니다.
 *
 * Service 계층에서는 이 DTO를 받아 Board 엔티티를 생성하거나 수정합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardReq {

    /**
     * 게시글의 제목.
     * - null 또는 공백일 수 없습니다.
     * - 게시글 목록 조회 시 검색 필터나 정렬 기준으로도 사용될 수 있습니다.
     */
    @NotBlank(message = "제목은 반드시 입력해야 합니다.")
    private String title;

    /**
     * 게시글의 본문 내용.
     * - null 또는 공백일 수 없습니다.
     * - HTML 태그나 스크립트 삽입은 서버단에서 XSS 필터링을 거칠 수 있습니다.
     */
    @NotBlank(message = "내용은 반드시 입력해야 합니다.")
    private String contents;

    /**
     * 게시글에 첨부된 대표 이미지 URL.
     * - 선택값이며, 사용자가 이미지 업로드 기능을 사용할 때 자동 저장될 수 있습니다.
     */
    private String imageUrl;
}
