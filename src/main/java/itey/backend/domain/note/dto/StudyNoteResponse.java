package itey.backend.domain.note.dto;

import itey.backend.domain.note.entity.StudyNote;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyNoteResponse {
    private UUID id;
    private UUID scheduleId;
    private String scheduleTitle;
    private String content;
    private String aiSummary;
    private String aiFeedback;
    private String imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StudyNoteResponse from(StudyNote note) {
        return new StudyNoteResponse(
                note.getId(),
                note.getSchedule().getId(),
                note.getSchedule().getTitle(),
                note.getContent(),
                note.getAiSummary(),
                note.getAiFeedback(),
                note.getImageUrls(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
