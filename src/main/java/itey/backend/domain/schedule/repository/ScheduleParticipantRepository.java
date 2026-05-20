package itey.backend.domain.schedule.repository;

import itey.backend.domain.schedule.entity.ScheduleParticipant;
import itey.backend.domain.schedule.entity.enums.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleParticipantRepository extends JpaRepository<ScheduleParticipant, UUID> {

    List<ScheduleParticipant> findByScheduleId(UUID scheduleId);

    Optional<ScheduleParticipant> findByScheduleIdAndUserId(UUID scheduleId, UUID userId);

    void deleteByScheduleId(UUID scheduleId);

    List<ScheduleParticipant> findByUserIdAndStatus(UUID userId, ParticipantStatus status);
}
