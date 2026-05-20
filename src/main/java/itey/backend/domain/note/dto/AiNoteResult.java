package itey.backend.domain.note.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiNoteResult {
    private String summary;
    private String feedback;
}
