package itey.backend.domain.admin.dto;

import itey.backend.domain.log.entity.ApiLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogResponse {
    private UUID id;
    private UUID userId;
    private String method;
    private String path;
    private int statusCode;
    private int responseMs;
    private String ipHash;
    private LocalDateTime createdAt;

    public static ApiLogResponse from(ApiLog log) {
        return new ApiLogResponse(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getMethod(),
                log.getPath(),
                log.getStatusCode() != null ? log.getStatusCode() : 0,
                log.getResponseMs() != null ? log.getResponseMs() : 0,
                log.getIpHash(),
                log.getCreatedAt()
        );
    }
}
