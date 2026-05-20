package itey.backend.domain.chat.dto;

import itey.backend.domain.chat.entity.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    private String content;
    private MessageType messageType;
    private UUID replyToId;
    private String mediaUrl;
}
