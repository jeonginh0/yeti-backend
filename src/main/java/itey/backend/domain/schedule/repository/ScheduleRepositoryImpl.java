package itey.backend.domain.schedule.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import itey.backend.domain.schedule.entity.QSchedule;
import itey.backend.domain.schedule.entity.QScheduleParticipant;
import itey.backend.domain.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QSchedule s = QSchedule.schedule;
    private static final QScheduleParticipant sp = QScheduleParticipant.scheduleParticipant;

    @Override
    public List<Schedule> findMySchedules(UUID userId, LocalDateTime from, LocalDateTime to, String category) {
        return queryFactory
                .selectDistinct(s)
                .from(s)
                .where(
                        ownerOrParticipant(userId),
                        categoryEq(category),
                        startAtGoe(from),
                        startAtLoe(to)
                )
                .orderBy(s.startAt.asc())
                .fetch();
    }

    private BooleanExpression ownerOrParticipant(UUID userId) {
        return s.owner.id.eq(userId).or(
                JPAExpressions.selectOne()
                        .from(sp)
                        .where(sp.schedule.eq(s).and(sp.user.id.eq(userId)))
                        .exists()
        );
    }

    private BooleanExpression categoryEq(String category) {
        return category != null ? s.category.eq(category) : null;
    }

    private BooleanExpression startAtGoe(LocalDateTime from) {
        return from != null ? s.startAt.goe(from) : null;
    }

    private BooleanExpression startAtLoe(LocalDateTime to) {
        return to != null ? s.startAt.loe(to) : null;
    }
}
