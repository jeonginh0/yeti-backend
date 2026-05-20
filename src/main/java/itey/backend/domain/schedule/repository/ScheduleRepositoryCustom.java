package itey.backend.domain.schedule.repository;

import itey.backend.domain.schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepositoryCustom {

    List<Schedule> findMySchedules(UUID userId, LocalDateTime from, LocalDateTime to, String category);
}
