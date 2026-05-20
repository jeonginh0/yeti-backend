package itey.backend.domain.chat.repository;

import itey.backend.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, UUID> {

    Optional<ChatRoomMember> findByRoomIdAndUserId(UUID roomId, UUID userId);

    List<ChatRoomMember> findByRoomId(UUID roomId);

    boolean existsByRoomIdAndUserId(UUID roomId, UUID userId);

    int countByRoomId(UUID roomId);
}
