package itey.backend.domain.notification.dto;

import itey.backend.domain.notification.entity.Notification;
import itey.backend.domain.notification.entity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private NotificationType type;
    private String title;
    private String body;
    private String data;
    private boolean read;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private UUID scheduleId;
    private String scheduleTitle;

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getData(),
                n.isRead(),
                n.getSentAt(),
                n.getReadAt(),
                n.getSchedule() != null ? n.getSchedule().getId() : null,
                n.getSchedule() != null ? n.getSchedule().getTitle() : null
        );
    }
}
