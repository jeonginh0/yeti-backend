package itey.backend.domain.log.entity;

import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_parse_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiParseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(columnDefinition = "text")
    private String rawInput;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parsed_result", columnDefinition = "jsonb")
    private String parsedResult;

    private Float confidence;
    private Integer tokensUsed;

    @Column(precision = 8, scale = 6)
    private BigDecimal costUsd;

    @Column(length = 20)
    private String planStatus;

    private Boolean success;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
