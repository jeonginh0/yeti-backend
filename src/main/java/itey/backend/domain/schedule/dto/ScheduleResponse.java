package itey.backend.domain.schedule.dto;

import itey.backend.domain.schedule.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private UUID id;
    private String title;
    private String category;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;
    private String location;
    private boolean completed;
    private String visibility;
    private boolean owner;

    public static ScheduleResponse from(Schedule schedule, UUID currentUserId) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getCategory(),
                schedule.getStartAt(),
                schedule.getEndAt(),
                schedule.isAllDay(),
                schedule.getLocation(),
                schedule.isCompleted(),
                schedule.getVisibility().name(),
                schedule.getOwner().getId().equals(currentUserId)
        );
    }
}
