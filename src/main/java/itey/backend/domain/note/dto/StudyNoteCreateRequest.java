package itey.backend.domain.note.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyNoteCreateRequest {
    @NotNull
    private UUID scheduleId;

    private String content;
    private List<String> imageUrls;
}
