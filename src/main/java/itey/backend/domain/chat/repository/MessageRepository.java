package itey.backend.domain.chat.repository;

import itey.backend.domain.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("""
            SELECT m FROM Message m
            WHERE m.room.id = :roomId AND m.createdAt < :before
            ORDER BY m.createdAt DESC
            """)
    List<Message> findBefore(
            @Param("roomId") UUID roomId,
            @Param("before") LocalDateTime before,
            Pageable pageable);

    @Query("""
            SELECT m FROM Message m
            WHERE m.room.id = :roomId
            ORDER BY m.createdAt DESC
            """)
    List<Message> findLatest(@Param("roomId") UUID roomId, Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.room.id = :roomId AND m.createdAt > :after
            """)
    long countUnread(@Param("roomId") UUID roomId, @Param("after") LocalDateTime after);
}
