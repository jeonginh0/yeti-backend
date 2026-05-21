package itey.backend.domain.chat.repository;

import itey.backend.domain.chat.entity.Message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepositoryCustom {
    List<Message> findBeforeWithSender(UUID roomId, LocalDateTime before, int size);
    List<Message> findLatestWithSender(UUID roomId, int size);
}
