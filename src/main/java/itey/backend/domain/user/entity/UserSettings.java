package itey.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private Integer notifyBeforeMin = 15;

    @Column(name = "notify_on_invite")
    @Builder.Default
    private boolean notifyOnInvite = true;

    @Column(name = "notify_on_chat")
    @Builder.Default
    private boolean notifyOnChat = true;

    @Column(length = 10)
    @Builder.Default
    private String theme = "system";

    @Column(length = 10)
    @Builder.Default
    private String language = "ko";

    private LocalDateTime updatedAt;

    public void update(Integer notifyBeforeMin, Boolean notifyOnInvite, Boolean notifyOnChat, String theme, String language) {
        if (notifyBeforeMin != null) this.notifyBeforeMin = notifyBeforeMin;
        if (notifyOnInvite != null) this.notifyOnInvite = notifyOnInvite;
        if (notifyOnChat != null) this.notifyOnChat = notifyOnChat;
        if (theme != null) this.theme = theme;
        if (language != null) this.language = language;
        this.updatedAt = LocalDateTime.now();
    }
}
