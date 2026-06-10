package itey.backend.domain.chat.controller;

import itey.backend.domain.chat.dto.ChatMessageRequest;
import itey.backend.domain.chat.dto.MessageResponse;
import itey.backend.domain.chat.service.ChatService;
import itey.backend.domain.chat.service.ChatNotificationService;
import itey.backend.domain.chat.service.RedisMessagePublisher;
import itey.backend.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
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
            Principal principal,
            ChatMessageRequest request) {
        // STOMP에서는 @AuthenticationPrincipal이 동작하지 않으므로, CONNECT 때 설정된 세션 사용자를 Principal로 받는다
        UserPrincipal user = extractUser(principal);
        MessageResponse response = chatService.saveMessage(roomId, user.getId(), request);
        publisher.publish(roomId, response);
        chatNotificationService.notifyRoomMembers(roomId, user.getId(), response);
    }

    private UserPrincipal extractUser(Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 WebSocket 연결입니다.");
    }
}
