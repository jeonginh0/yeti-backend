package itey.backend.domain.notification.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import itey.backend.domain.notification.entity.Notification;
import itey.backend.domain.notification.entity.QNotification;
import itey.backend.domain.schedule.entity.QSchedule;
import itey.backend.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QNotification n = QNotification.notification;
    private static final QSchedule s = QSchedule.schedule;
    private static final QUser u = QUser.user;

    @Override
    public List<Notification> findByUserIdWithSchedule(UUID userId, boolean unreadOnly, int size, UUID beforeId) {
        var query = queryFactory
                .selectFrom(n)
                .leftJoin(n.schedule, s).fetchJoin()
                .where(n.user.id.eq(userId));

        if (unreadOnly) {
            query.where(n.read.isFalse());
        }

        if (beforeId != null) {
            var cursor = queryFactory.selectFrom(n).where(n.id.eq(beforeId)).fetchOne();
            if (cursor != null) {
                query.where(n.sentAt.lt(cursor.getSentAt()));
            }
        }

        return query
                .orderBy(n.sentAt.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public long countUnread(UUID userId) {
        Long count = queryFactory
                .select(n.count())
                .from(n)
                .where(n.user.id.eq(userId).and(n.read.isFalse()))
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Optional<Notification> findByIdWithUser(UUID notificationId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(n)
                        .join(n.user, u).fetchJoin()
                        .leftJoin(n.schedule, s).fetchJoin()
                        .where(n.id.eq(notificationId))
                        .fetchOne()
        );
    }
}
