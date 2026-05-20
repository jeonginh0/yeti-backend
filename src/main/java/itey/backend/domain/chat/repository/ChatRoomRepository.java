package itey.backend.domain.chat.repository;

import itey.backend.domain.chat.entity.ChatRoom;
import itey.backend.domain.chat.entity.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    @Query("""
            SELECT r FROM ChatRoom r
            JOIN ChatRoomMember m ON m.room = r
            WHERE m.user.id = :userId AND r.active = true
            ORDER BY r.createdAt DESC
            """)
    List<ChatRoom> findRoomsByUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT m1.room FROM ChatRoomMember m1
            JOIN ChatRoomMember m2 ON m1.room = m2.room
            WHERE m1.user.id = :userId1 AND m2.user.id = :userId2
            AND m1.room.type = :type
            """)
    Optional<ChatRoom> findRoomByTwoUsersAndType(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2,
            @Param("type") ChatRoomType type);
}
