package itey.backend.domain.schedule.repository;

import itey.backend.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID>, ScheduleRepositoryCustom {

    List<Schedule> findByStartAtBetween(LocalDateTime from, LocalDateTime to);

    long countByCreatedAtGreaterThanEqual(LocalDateTime from);
}
