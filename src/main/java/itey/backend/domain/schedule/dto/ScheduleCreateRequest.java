package itey.backend.domain.schedule.dto;

import itey.backend.domain.schedule.entity.enums.VisibilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequest {
    @NotBlank private String title;
    private String description;
    private String category;
    @NotNull private LocalDateTime startAt;
    @NotNull private LocalDateTime endAt;
    private boolean allDay;
    private String location;
    private boolean recurring;
    private String recurrenceRule;
    private VisibilityType visibility;
    private List<String> participantUsernames;
}
