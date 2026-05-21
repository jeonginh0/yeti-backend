package itey.backend.domain.chat.repository;

import itey.backend.domain.chat.dto.ChatRoomSummary;

import java.util.List;
import java.util.UUID;

public interface ChatRoomRepositoryCustom {
    List<ChatRoomSummary> findRoomSummariesByUserId(UUID userId);
}
