package itey.backend.domain.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ParsedScheduleResult {
    private String title;
    private String category;
    private String startAt;
    private String endAt;
    private List<String> participants;
    private String location;
    private Float aiConfidence;
}
