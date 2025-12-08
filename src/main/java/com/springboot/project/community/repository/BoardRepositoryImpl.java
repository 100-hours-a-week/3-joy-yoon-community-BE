package com.springboot.project.community.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.springboot.project.community.entity.Board;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.springboot.project.community.entity.QBoard.board;
import static com.springboot.project.community.entity.QUser.user;
import static com.springboot.project.community.entity.QBoardStats.boardStats;

@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Board> findBoardsByInfiniteScroll(Long lastPostId, int pageSize) {
        return queryFactory
                .selectFrom(board)
                // 작성자 정보 조인 (N+1 방지 + 즉시 로딩)
                .leftJoin(board.author, user).fetchJoin()
                // 게시물 통계 정보 조인
                .leftJoin(board.boardStats, boardStats).fetchJoin()
                .where(
                        ltPostId(lastPostId) // 무한 스크롤 커서 조건
                )
                .orderBy(board.postId.desc()) // 최신 게시물 먼저
                .limit(pageSize) // 요청한 개수만큼만 조회
                .fetch();
    }

    /**
     * 무한 스크롤 커서 조건
     * - 첫 페이지(lastPostId가 null)인 경우 조건을 추가하지 않음
     * - 이후 페이지는 현재 마지막 postId보다 작은 게시물만 조회
     */
    private BooleanExpression ltPostId(Long lastPostId) {
        if (lastPostId == null) {
            return null; // QueryDSL은 null 조건을 무시함
        }
        return board.postId.lt(lastPostId);
    }
}
