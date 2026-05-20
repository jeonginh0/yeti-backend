package itey.backend.domain.schedule.dto;

import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.schedule.entity.ScheduleParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailResponse {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;
    private String location;
    private boolean recurring;
    private String recurrenceRule;
    private boolean completed;
    private LocalDateTime completedAt;
    private String visibility;
    private String sharedFields;
    private Float aiConfidence;
    private UUID ownerId;
    private String ownerUsername;
    private String ownerNickname;
    private String ownerProfileImageUrl;
    private List<ParticipantResponse> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleDetailResponse from(Schedule schedule, List<ScheduleParticipant> participants) {
        return new ScheduleDetailResponse(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getCategory(),
                schedule.getStartAt(),
                schedule.getEndAt(),
                schedule.isAllDay(),
                schedule.getLocation(),
                schedule.isRecurring(),
                schedule.getRecurrenceRule(),
                schedule.isCompleted(),
                schedule.getCompletedAt(),
                schedule.getVisibility().name(),
                schedule.getSharedFields(),
                schedule.getAiConfidence(),
                schedule.getOwner().getId(),
                schedule.getOwner().getUsername(),
                schedule.getOwner().getNickname(),
                schedule.getOwner().getProfileImageUrl(),
                participants.stream().map(ParticipantResponse::from).toList(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
