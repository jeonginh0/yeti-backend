package itey.backend.domain.log.repository;

import itey.backend.domain.log.entity.AiParseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AiParseLogRepository extends JpaRepository<AiParseLog, UUID> {

    @Query("SELECT COALESCE(SUM(a.costUsd), 0) FROM AiParseLog a WHERE a.createdAt >= :from")
    BigDecimal sumCostUsdSince(@Param("from") LocalDateTime from);
}
