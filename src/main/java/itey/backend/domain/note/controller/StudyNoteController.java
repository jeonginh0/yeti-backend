package itey.backend.domain.note.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itey.backend.domain.note.dto.StudyNoteCreateRequest;
import itey.backend.domain.note.dto.StudyNoteResponse;
import itey.backend.domain.note.service.StudyNoteService;
import itey.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "StudyNote", description = "학습 노트 API")
@RestController
@RequestMapping("/api/study-notes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudyNoteController {

    private final StudyNoteService studyNoteService;

    @Operation(summary = "학습 노트 작성")
    @PostMapping
    public ResponseEntity<StudyNoteResponse> createNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StudyNoteCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studyNoteService.createNote(principal.getId(), request));
    }

    @Operation(summary = "일정별 학습 노트 조회")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<List<StudyNoteResponse>> getNotesBySchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID scheduleId) {
        return ResponseEntity.ok(studyNoteService.getNotesBySchedule(scheduleId, principal.getId()));
    }

    @Operation(summary = "AI 3줄 요약")
    @PostMapping("/{noteId}/summarize")
    public ResponseEntity<StudyNoteResponse> summarize(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId) {
        return ResponseEntity.ok(studyNoteService.summarize(noteId, principal.getId()));
    }
}
