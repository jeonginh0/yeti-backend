package itey.backend.domain.log.entity;

import itey.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 10)
    private String method;

    @Column(columnDefinition = "text")
    private String path;

    private Integer statusCode;
    private Integer responseMs;

    @Column(length = 64)
    private String ipHash;

    @Column(columnDefinition = "text")
    private String userAgent;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
