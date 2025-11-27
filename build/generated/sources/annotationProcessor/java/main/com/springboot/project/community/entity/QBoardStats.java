package com.springboot.project.community.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoardStats is a Querydsl query type for BoardStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoardStats extends EntityPathBase<BoardStats> {

    private static final long serialVersionUID = 229827688L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoardStats boardStats = new QBoardStats("boardStats");

    public final QBoard board;

    public final NumberPath<Long> commentCount = createNumber("commentCount", Long.class);

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final NumberPath<Long> postId = createNumber("postId", Long.class);

    public final NumberPath<Long> viewCount = createNumber("viewCount", Long.class);

    public QBoardStats(String variable) {
        this(BoardStats.class, forVariable(variable), INITS);
    }

    public QBoardStats(Path<? extends BoardStats> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoardStats(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoardStats(PathMetadata metadata, PathInits inits) {
        this(BoardStats.class, metadata, inits);
    }

    public QBoardStats(Class<? extends BoardStats> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new QBoard(forProperty("board"), inits.get("board")) : null;
    }

}

