package itey.backend.domain.log.repository;

import itey.backend.domain.log.entity.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ApiLogRepository extends JpaRepository<ApiLog, UUID> {
    Page<ApiLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT a.user.id) FROM ApiLog a WHERE a.createdAt >= :from")
    long countDistinctUsersSince(@Param("from") LocalDateTime from);
}
