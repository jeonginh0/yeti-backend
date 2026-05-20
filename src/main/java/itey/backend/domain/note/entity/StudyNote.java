package itey.backend.domain.note.entity;

import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.user.entity.User;
import itey.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "study_notes")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyNote extends BaseTimeEntity {

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

    @Column(columnDefinition = "text")
    private String content;

    @Column(columnDefinition = "text")
    private String aiSummary;

    @Column(columnDefinition = "text")
    private String aiFeedback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "jsonb")
    @Builder.Default
    private String imageUrls = "[]";

    public void updateContent(String content) {
        this.content = content;
    }

    public void saveAiResult(String summary, String feedback) {
        this.aiSummary = summary;
        this.aiFeedback = feedback;
    }
}
