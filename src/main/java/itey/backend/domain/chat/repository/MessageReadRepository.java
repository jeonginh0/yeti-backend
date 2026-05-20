package itey.backend.domain.chat.repository;

import itey.backend.domain.chat.entity.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageReadRepository extends JpaRepository<MessageRead, UUID> {
}
