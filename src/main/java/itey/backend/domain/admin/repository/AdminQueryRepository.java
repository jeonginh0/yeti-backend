package itey.backend.domain.admin.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import itey.backend.domain.schedule.entity.QSchedule;
import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.user.entity.QUser;
import itey.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AdminQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QUser u = QUser.user;
    private static final QSchedule s = QSchedule.schedule;

    public Page<User> findUsers(String keyword, Boolean active, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            where.and(u.username.containsIgnoreCase(keyword)
                    .or(u.email.containsIgnoreCase(keyword))
                    .or(u.nickname.containsIgnoreCase(keyword)));
        }
        if (active != null) {
            where.and(u.active.eq(active));
        }

        List<User> users = queryFactory.selectFrom(u)
                .where(where)
                .orderBy(u.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(u.count()).from(u).where(where).fetchOne();
        return new PageImpl<>(users, pageable, total != null ? total : 0L);
    }

    public Page<Schedule> findSchedules(UUID ownerId, LocalDateTime from, LocalDateTime to,
                                        Boolean completed, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();
        if (ownerId != null) {
            where.and(s.owner.id.eq(ownerId));
        }
        if (from != null) {
            where.and(s.startAt.goe(from));
        }
        if (to != null) {
            where.and(s.startAt.loe(to));
        }
        if (completed != null) {
            where.and(s.completed.eq(completed));
        }

        List<Schedule> schedules = queryFactory.selectFrom(s)
                .join(s.owner, u).fetchJoin()
                .where(where)
                .orderBy(s.startAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory.select(s.count()).from(s).where(where).fetchOne();
        return new PageImpl<>(schedules, pageable, total != null ? total : 0L);
    }
}
