package itey.backend.domain.admin.dto;

import itey.backend.domain.log.entity.enums.ReportStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequest {
    private ReportStatus status;
}
