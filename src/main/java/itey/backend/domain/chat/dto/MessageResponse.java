package itey.backend.domain.chat.dto;

import itey.backend.domain.chat.entity.Message;
import itey.backend.domain.chat.entity.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private UUID id;
    private UUID senderId;
    private String senderNickname;
    private String senderProfileUrl;
    private MessageType messageType;
    private String content;
    private String mediaUrl;
    private String reactions;
    private UUID replyToId;
    private boolean deleted;
    private LocalDateTime createdAt;

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getSender().getProfileImageUrl(),
                message.getMessageType(),
                message.getContent(),
                message.getMediaUrl(),
                message.getReactions(),
                message.getReplyTo() != null ? message.getReplyTo().getId() : null,
                message.isDeleted(),
                message.getCreatedAt()
        );
    }
}
