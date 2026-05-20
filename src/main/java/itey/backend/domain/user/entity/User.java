package itey.backend.domain.user.entity;

import itey.backend.domain.user.entity.enums.PlanStatus;
import itey.backend.domain.user.entity.enums.Provider;
import itey.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 50)
    private String username;

    @Column(length = 50)
    private String nickname;

    @Column(columnDefinition = "text")
    private String profileImageUrl;

    @Column(length = 100)
    private String statusMessage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Provider provider;

    @Column(length = 255)
    private String providerId;

    @Column(columnDefinition = "text")
    private String passwordHash;

    @Column(columnDefinition = "text")
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PlanStatus planStatus = PlanStatus.TRIAL;

    private LocalDateTime trialStartedAt;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_admin")
    @Builder.Default
    private boolean admin = false;

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updatePlanStatus(PlanStatus planStatus) {
        this.planStatus = planStatus;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void deactivate() {
        this.active = false;
    }
}
