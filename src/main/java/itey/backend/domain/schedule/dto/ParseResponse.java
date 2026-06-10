package itey.backend.domain.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParseResponse {
    private String title;
    private String category;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private List<String> participants;
    private String location;
    private boolean recurring;
    private String recurrenceRule;
    private Float aiConfidence;
    private boolean clarificationRequired;
    private String transcribedText;
}
