package itey.backend.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import itey.backend.domain.chat.dto.ChatMessageRequest;
import itey.backend.domain.chat.dto.ChatRoomCreateRequest;
import itey.backend.domain.chat.dto.ChatRoomResponse;
import itey.backend.domain.chat.dto.MessageResponse;
import itey.backend.domain.chat.entity.ChatRoom;
import itey.backend.domain.chat.entity.ChatRoomMember;
import itey.backend.domain.chat.entity.Message;
import itey.backend.domain.chat.entity.enums.ChatRoomRole;
import itey.backend.domain.chat.entity.enums.ChatRoomType;
import itey.backend.domain.chat.entity.enums.MessageType;
import itey.backend.domain.chat.repository.ChatRoomMemberRepository;
import itey.backend.domain.chat.repository.ChatRoomRepository;
import itey.backend.domain.chat.repository.MessageRepository;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.entity.enums.FriendshipStatus;
import itey.backend.domain.user.repository.FriendshipRepository;
import itey.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(UUID userId) {
        return chatRoomRepository.findRoomSummariesByUserId(userId).stream()
                .map(s -> ChatRoomResponse.of(
                        s.getRoom(),
                        s.getMemberCount() != null ? s.getMemberCount().intValue() : 0,
                        s.getUnreadCount() != null ? s.getUnreadCount() : 0L))
                .toList();
    }

    public ChatRoomResponse createRoom(UUID userId, ChatRoomCreateRequest req) {
        User creator = findUser(userId);

        if (req.getType() == ChatRoomType.DIRECT) {
            return createDirectRoom(creator, req);
        }
        return createGroupRoom(creator, req);
    }

    private ChatRoomResponse createDirectRoom(User creator, ChatRoomCreateRequest req) {
        if (req.getMemberUsernames().size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DM은 상대방 1명만 지정해야 합니다.");
        }

        String targetUsername = req.getMemberUsernames().get(0);
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + targetUsername));

        checkFriendship(creator.getId(), target.getId());

        Optional<ChatRoom> existing = chatRoomRepository.findRoomByTwoUsersAndType(
                creator.getId(), target.getId(), ChatRoomType.DIRECT);

        if (existing.isPresent()) {
            ChatRoom room = existing.get();
            int memberCount = chatRoomMemberRepository.countByRoomId(room.getId());
            return ChatRoomResponse.of(room, memberCount, 0);
        }

        ChatRoom room = ChatRoom.builder()
                .type(ChatRoomType.DIRECT)
                .createdBy(creator)
                .build();
        chatRoomRepository.save(room);

        saveMember(room, creator, ChatRoomRole.OWNER);
        saveMember(room, target, ChatRoomRole.MEMBER);

        return ChatRoomResponse.of(room, 2, 0);
    }

    private ChatRoomResponse createGroupRoom(User creator, ChatRoomCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "그룹 채팅방은 이름이 필요합니다.");
        }

        ChatRoom room = ChatRoom.builder()
                .type(ChatRoomType.GROUP)
                .name(req.getName())
                .createdBy(creator)
                .build();
        chatRoomRepository.save(room);

        saveMember(room, creator, ChatRoomRole.OWNER);

        for (String username : req.getMemberUsernames()) {
            User member = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + username));
            checkFriendship(creator.getId(), member.getId());
            saveMember(room, member, ChatRoomRole.MEMBER);
        }

        int memberCount = 1 + req.getMemberUsernames().size();
        return ChatRoomResponse.of(room, memberCount, 0);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID roomId, UUID userId, UUID beforeId, int size) {
        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다.");
        }

        List<Message> messages;

        if (beforeId != null) {
            Message cursor = messageRepository.findById(beforeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."));
            messages = messageRepository.findBeforeWithSender(roomId, cursor.getCreatedAt(), size);
        } else {
            messages = messageRepository.findLatestWithSender(roomId, size);
        }

        return messages.stream().map(MessageResponse::from).toList();
    }

    public MessageResponse addReaction(UUID messageId, UUID userId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(message.getRoom().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다.");
        }

        Map<String, List<String>> reactions = parseReactions(message.getReactions());
        String userIdStr = userId.toString();

        reactions.compute(emoji, (key, users) -> {
            if (users == null) users = new ArrayList<>();
            if (users.contains(userIdStr)) {
                users.remove(userIdStr);
            } else {
                users.add(userIdStr);
            }
            return users.isEmpty() ? null : users;
        });

        message.updateReactions(toJson(reactions));
        return MessageResponse.from(message);
    }

    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        if (!message.getSender().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 메시지만 삭제할 수 있습니다.");
        }

        message.softDelete();
    }

    private void saveMember(ChatRoom room, User user, ChatRoomRole role) {
        chatRoomMemberRepository.save(
                ChatRoomMember.builder()
                        .room(room)
                        .user(user)
                        .role(role)
                        .build()
        );
    }

    private void checkFriendship(UUID userId, UUID targetId) {
        boolean areFriends = friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(userId, targetId, FriendshipStatus.ACCEPTED)
                || friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(targetId, userId, FriendshipStatus.ACCEPTED);
        if (!areFriends) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "친구 관계가 아닌 사용자입니다.");
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private Map<String, List<String>> parseReactions(String json) {
        if (json == null || json.isBlank() || "{}".equals(json)) return new LinkedHashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new LinkedHashMap<>();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public MessageResponse saveMessage(UUID roomId, UUID senderId, ChatMessageRequest req) {
        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, senderId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "채팅방 멤버가 아닙니다.");
        }

        User sender = findUser(senderId);
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        Message replyTo = null;
        if (req.getReplyToId() != null) {
            replyTo = messageRepository.findById(req.getReplyToId()).orElse(null);
        }

        Message message = Message.builder()
                .room(room)
                .sender(sender)
                .replyTo(replyTo)
                .messageType(req.getMessageType() != null ? req.getMessageType() : MessageType.TEXT)
                .content(req.getContent())
                .mediaUrl(req.getMediaUrl())
                .build();

        messageRepository.save(message);
        return MessageResponse.from(message);
    }

    public void updateLastRead(UUID roomId, UUID userId) {
        chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .ifPresent(ChatRoomMember::updateLastRead);
    }
}
