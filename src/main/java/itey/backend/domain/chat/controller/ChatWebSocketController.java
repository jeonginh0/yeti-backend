package itey.backend.domain.chat.controller;

import itey.backend.domain.chat.dto.ChatMessageRequest;
import itey.backend.domain.chat.dto.MessageResponse;
import itey.backend.domain.chat.service.ChatService;
import itey.backend.domain.chat.service.ChatNotificationService;
import itey.backend.domain.chat.service.RedisMessagePublisher;
import itey.backend.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final RedisMessagePublisher publisher;
    private final ChatNotificationService chatNotificationService;

    @MessageMapping("/chat/rooms/{roomId}/send")
    public void sendMessage(
            @DestinationVariable UUID roomId,
            @AuthenticationPrincipal UserPrincipal principal,
            ChatMessageRequest request) {
        MessageResponse response = chatService.saveMessage(roomId, principal.getId(), request);
        publisher.publish(roomId, response);
        chatNotificationService.notifyRoomMembers(roomId, principal.getId(), response);
    }
}
