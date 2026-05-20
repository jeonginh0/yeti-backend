package itey.backend.domain.schedule.entity;

import itey.backend.domain.schedule.entity.enums.ParticipantStatus;
import itey.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_participants")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduleParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.PENDING;

    private LocalDateTime adjustProposedStart;
    private LocalDateTime adjustProposedEnd;
    private LocalDateTime respondedAt;
    private LocalDateTime notifiedAt;

    public void accept() {
        this.status = ParticipantStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = ParticipantStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void requestAdjust(LocalDateTime proposedStart, LocalDateTime proposedEnd) {
        this.status = ParticipantStatus.ADJUST_REQUESTED;
        this.adjustProposedStart = proposedStart;
        this.adjustProposedEnd = proposedEnd;
        this.respondedAt = LocalDateTime.now();
    }
}
