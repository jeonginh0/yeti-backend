package itey.backend.domain.chat.service;

import itey.backend.domain.chat.dto.MessageResponse;
import itey.backend.domain.chat.entity.ChatRoomMember;
import itey.backend.domain.chat.repository.ChatRoomMemberRepository;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.repository.UserRepository;
import itey.backend.global.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatNotificationService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    public void notifyRoomMembers(UUID roomId, UUID senderId, MessageResponse message) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByRoomId(roomId);

        List<String> tokens = members.stream()
                .filter(m -> !m.getUser().getId().equals(senderId))
                .filter(m -> !m.isMuted())
                .map(m -> userRepository.findById(m.getUser().getId()).orElse(null))
                .filter(u -> u != null && u.getFcmToken() != null)
                .map(User::getFcmToken)
                .toList();

        if (tokens.isEmpty()) return;

        String preview = message.getContent() != null
                ? (message.getContent().length() > 50
                    ? message.getContent().substring(0, 50) + "..."
                    : message.getContent())
                : "미디어 파일";

        fcmService.sendToTokens(tokens,
                message.getSenderNickname(),
                preview,
                Map.of(
                        "type", "CHAT_MESSAGE",
                        "roomId", roomId.toString(),
                        "messageId", message.getId().toString()
                ));
    }
}
