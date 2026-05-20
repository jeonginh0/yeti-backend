package itey.backend.domain.note.repository;

import itey.backend.domain.note.entity.StudyNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyNoteRepository extends JpaRepository<StudyNote, UUID> {

    List<StudyNote> findByScheduleIdAndUserId(UUID scheduleId, UUID userId);

    Optional<StudyNote> findByIdAndUserId(UUID id, UUID userId);
}
