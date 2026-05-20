package itey.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PlanStatusResponse {
    private String planStatus;
    private LocalDateTime trialEndsAt;
    private LocalDateTime subscriptionExpiresAt;
}
