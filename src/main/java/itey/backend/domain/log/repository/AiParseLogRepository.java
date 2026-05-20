package itey.backend.domain.log.repository;

import itey.backend.domain.log.entity.AiParseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiParseLogRepository extends JpaRepository<AiParseLog, UUID> {
}
