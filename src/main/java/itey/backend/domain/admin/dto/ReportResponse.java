package itey.backend.domain.admin.dto;

import itey.backend.domain.log.entity.Report;
import itey.backend.domain.log.entity.enums.ReportStatus;
import itey.backend.domain.log.entity.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private UUID id;
    private UUID reporterId;
    private String reporterUsername;
    private ReportTargetType targetType;
    private UUID targetId;
    private String reason;
    private ReportStatus status;
    private UUID reviewedById;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getUsername(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getReviewedBy() != null ? report.getReviewedBy().getId() : null,
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }
}
