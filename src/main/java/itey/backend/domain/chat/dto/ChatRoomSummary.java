package itey.backend.domain.chat.dto;

import itey.backend.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomSummary {
    private ChatRoom room;
    private Long memberCount;
    private Long unreadCount;
}
