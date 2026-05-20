package itey.backend.domain.schedule.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustRequest {
    @NotNull private LocalDateTime proposedStart;
    @NotNull private LocalDateTime proposedEnd;
}
