package itey.backend.domain.admin.dto;

import itey.backend.domain.log.entity.AiParseLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiLogResponse {
    private UUID id;
    private UUID userId;
    private String rawInput;
    private Float confidence;
    private Integer tokensUsed;
    private BigDecimal costUsd;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static AiLogResponse from(AiParseLog log) {
        return new AiLogResponse(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getRawInput(),
                log.getConfidence(),
                log.getTokensUsed(),
                log.getCostUsd(),
                log.getSuccess(),
                log.getErrorMessage(),
                log.getCreatedAt()
        );
    }
}
