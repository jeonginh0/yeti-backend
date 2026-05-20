package itey.backend.domain.schedule.dto;

import itey.backend.domain.schedule.entity.ScheduleParticipant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {

    private UUID participantId;
    private UUID scheduleId;
    private String scheduleTitle;
    private String category;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean allDay;
    private String location;
    private UUID ownerId;
    private String ownerNickname;
    private String ownerUsername;
    private String status;
    private LocalDateTime invitedAt;

    public static InvitationResponse from(ScheduleParticipant participant) {
        var schedule = participant.getSchedule();
        var owner = schedule.getOwner();
        return new InvitationResponse(
                participant.getId(),
                schedule.getId(),
                schedule.getTitle(),
                schedule.getCategory(),
                schedule.getStartAt(),
                schedule.getEndAt(),
                schedule.isAllDay(),
                schedule.getLocation(),
                owner.getId(),
                owner.getNickname(),
                owner.getUsername(),
                participant.getStatus().name(),
                participant.getNotifiedAt()
        );
    }
}
