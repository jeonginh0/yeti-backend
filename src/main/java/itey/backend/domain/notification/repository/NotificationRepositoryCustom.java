package itey.backend.domain.notification.repository;

import itey.backend.domain.notification.entity.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryCustom {
    List<Notification> findByUserIdWithSchedule(UUID userId, boolean unreadOnly, int size, UUID beforeId);
    long countUnread(UUID userId);
    Optional<Notification> findByIdWithUser(UUID notificationId);
}
