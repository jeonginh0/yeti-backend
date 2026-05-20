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
public class ParticipantResponse {
    private UUID userId;
    private String username;
    private String nickname;
    private String profileImageUrl;
    private String status;
    private LocalDateTime respondedAt;
    private LocalDateTime adjustProposedStart;
    private LocalDateTime adjustProposedEnd;

    public static ParticipantResponse from(ScheduleParticipant sp) {
        return new ParticipantResponse(
                sp.getUser().getId(),
                sp.getUser().getUsername(),
                sp.getUser().getNickname(),
                sp.getUser().getProfileImageUrl(),
                sp.getStatus().name(),
                sp.getRespondedAt(),
                sp.getAdjustProposedStart(),
                sp.getAdjustProposedEnd()
        );
    }
}
