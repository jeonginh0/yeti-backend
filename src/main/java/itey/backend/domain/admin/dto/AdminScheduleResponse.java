package itey.backend.domain.admin.dto;

import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.schedule.entity.enums.VisibilityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminScheduleResponse {
    private UUID id;
    private UUID ownerId;
    private String ownerUsername;
    private String title;
    private String category;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;
    private boolean recurring;
    private boolean completed;
    private VisibilityType visibility;
    private LocalDateTime createdAt;

    public static AdminScheduleResponse from(Schedule s) {
        return new AdminScheduleResponse(
                s.getId(),
                s.getOwner().getId(),
                s.getOwner().getUsername(),
                s.getTitle(),
                s.getCategory(),
                s.getStartAt(),
                s.getEndAt(),
                s.isAllDay(),
                s.isRecurring(),
                s.isCompleted(),
                s.getVisibility(),
                s.getCreatedAt()
        );
    }
}
