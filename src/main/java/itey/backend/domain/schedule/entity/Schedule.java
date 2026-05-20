package itey.backend.domain.schedule.entity;

import itey.backend.domain.schedule.entity.enums.VisibilityType;
import itey.backend.domain.user.entity.User;
import itey.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedules")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Schedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 50)
    private String category;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(name = "is_all_day")
    @Builder.Default
    private boolean allDay = false;

    @Column(length = 255)
    private String location;

    @Column(name = "is_recurring")
    @Builder.Default
    private boolean recurring = false;

    @Column(columnDefinition = "text")
    private String recurrenceRule;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private VisibilityType visibility = VisibilityType.FRIENDS;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shared_fields", columnDefinition = "jsonb")
    @Builder.Default
    private String sharedFields = "{}";

    @Column(name = "is_completed")
    @Builder.Default
    private boolean completed = false;

    private LocalDateTime completedAt;

    @Column(columnDefinition = "text")
    private String rawInput;

    private Float aiConfidence;

    public void update(String title, String description, String category,
                       LocalDateTime startAt, LocalDateTime endAt, boolean allDay,
                       String location, boolean recurring, String recurrenceRule,
                       VisibilityType visibility) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allDay = allDay;
        this.location = location;
        this.recurring = recurring;
        this.recurrenceRule = recurrenceRule;
        this.visibility = visibility;
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }
}
