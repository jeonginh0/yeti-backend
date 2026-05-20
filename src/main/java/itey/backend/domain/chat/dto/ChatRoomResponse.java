package itey.backend.domain.chat.dto;

import itey.backend.domain.chat.entity.ChatRoom;
import itey.backend.domain.chat.entity.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    private UUID id;
    private String name;
    private ChatRoomType type;
    private String thumbnailUrl;
    private int memberCount;
    private long unreadCount;
    private LocalDateTime createdAt;

    public static ChatRoomResponse of(ChatRoom room, int memberCount, long unreadCount) {
        return new ChatRoomResponse(
                room.getId(),
                room.getName(),
                room.getType(),
                room.getThumbnailUrl(),
                memberCount,
                unreadCount,
                room.getCreatedAt()
        );
    }
}
