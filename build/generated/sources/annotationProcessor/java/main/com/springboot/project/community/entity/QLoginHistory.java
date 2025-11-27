package com.springboot.project.community.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLoginHistory is a Querydsl query type for LoginHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLoginHistory extends EntityPathBase<LoginHistory> {

    private static final long serialVersionUID = -202625990L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLoginHistory loginHistory = new QLoginHistory("loginHistory");

    public final StringPath failureReason = createString("failureReason");

    public final NumberPath<Long> historyId = createNumber("historyId", Long.class);

    public final StringPath ipAddress = createString("ipAddress");

    public final DateTimePath<java.time.LocalDateTime> loginAt = createDateTime("loginAt", java.time.LocalDateTime.class);

    public final EnumPath<LoginHistory.LoginStatus> loginStatus = createEnum("loginStatus", LoginHistory.LoginStatus.class);

    public final DateTimePath<java.time.LocalDateTime> logoutAt = createDateTime("logoutAt", java.time.LocalDateTime.class);

    public final QUser user;

    public final StringPath userAgent = createString("userAgent");

    public QLoginHistory(String variable) {
        this(LoginHistory.class, forVariable(variable), INITS);
    }

    public QLoginHistory(Path<? extends LoginHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLoginHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLoginHistory(PathMetadata metadata, PathInits inits) {
        this(LoginHistory.class, metadata, inits);
    }

    public QLoginHistory(Class<? extends LoginHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

