package itey.backend.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import itey.backend.domain.chat.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String roomId = channel.substring("chat:room:".length());

        try {
            MessageResponse response = objectMapper.readValue(new String(message.getBody()), MessageResponse.class);
            messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, response);
        } catch (JsonProcessingException e) {
            log.error("메시지 역직렬화 실패 - channel: {}", channel, e);
        }
    }
}
