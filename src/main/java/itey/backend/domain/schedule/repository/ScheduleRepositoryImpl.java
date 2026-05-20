package itey.backend.domain.schedule.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import itey.backend.domain.schedule.entity.QSchedule;
import itey.backend.domain.schedule.entity.QScheduleParticipant;
import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.schedule.entity.enums.ParticipantStatus;
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
                        dateRangeFilter(from, to)
                )
                .orderBy(s.startAt.asc())
                .fetch();
    }

    private BooleanExpression ownerOrParticipant(UUID userId) {
        return s.owner.id.eq(userId).or(
                JPAExpressions.selectOne()
                        .from(sp)
                        .where(sp.schedule.eq(s)
                                .and(sp.user.id.eq(userId))
                                .and(sp.status.eq(ParticipantStatus.ACCEPTED)))
                        .exists()
        );
    }

    private BooleanExpression categoryEq(String category) {
        return category != null ? s.category.eq(category) : null;
    }

    private BooleanExpression dateRangeFilter(LocalDateTime from, LocalDateTime to) {
        // 반복 일정: 시작일이 조회 범위 종료 이전이면 포함 (프론트에서 RRULE 기반 확장)
        BooleanExpression recurring = s.recurring.isTrue()
                .and(to != null ? s.startAt.loe(to) : null);

        // 일반 일정: startAt이 조회 범위 내에 있어야 함
        BooleanExpression nonRecurring = s.recurring.isFalse()
                .and(from != null ? s.startAt.goe(from) : null)
                .and(to != null ? s.startAt.loe(to) : null);

        return recurring.or(nonRecurring);
    }
}
