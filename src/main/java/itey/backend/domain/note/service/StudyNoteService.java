package itey.backend.domain.note.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import itey.backend.domain.note.dto.AiNoteResult;
import itey.backend.domain.note.dto.StudyNoteCreateRequest;
import itey.backend.domain.note.dto.StudyNoteResponse;
import itey.backend.domain.note.entity.StudyNote;
import itey.backend.domain.note.repository.StudyNoteRepository;
import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.schedule.repository.ScheduleRepository;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyNoteService {

    private final StudyNoteRepository studyNoteRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public StudyNoteResponse createNote(UUID userId, StudyNoteCreateRequest req) {
        User user = findUser(userId);
        Schedule schedule = findSchedule(req.getScheduleId());

        if (!"학습".equals(schedule.getCategory())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "학습 카테고리 일정에만 노트를 작성할 수 있습니다.");
        }
        if (!schedule.isCompleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "완료된 일정에만 노트를 작성할 수 있습니다.");
        }

        String imageUrlsJson = toJson(req.getImageUrls());

        StudyNote note = StudyNote.builder()
                .schedule(schedule)
                .user(user)
                .content(req.getContent())
                .imageUrls(imageUrlsJson)
                .build();

        studyNoteRepository.save(note);
        return StudyNoteResponse.from(note);
    }

    @Transactional(readOnly = true)
    public List<StudyNoteResponse> getNotesBySchedule(UUID scheduleId, UUID userId) {
        return studyNoteRepository.findByScheduleIdAndUserId(scheduleId, userId).stream()
                .map(StudyNoteResponse::from)
                .toList();
    }

    public StudyNoteResponse summarize(UUID noteId, UUID userId) {
        StudyNote note = studyNoteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));

        if (note.getContent() == null || note.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내용이 없는 노트는 요약할 수 없습니다.");
        }

        BeanOutputConverter<AiNoteResult> converter = new BeanOutputConverter<>(AiNoteResult.class);

        String systemPrompt = """
                당신은 학습 내용 분석 전문가입니다. 사용자가 작성한 학습 노트를 분석하여 다음 두 가지를 제공하세요.

                1. summary: 핵심 내용을 3줄로 요약. 각 줄은 '•'로 시작.
                2. feedback: 학습 내용에 대한 건설적인 피드백. 잘 정리된 부분, 보완하면 좋을 점, 추가 학습을 추천할 개념을 2~4문장으로 작성.
                """;

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(note.getContent() + "\n\n" + converter.getFormat())
                .call()
                .content();

        AiNoteResult result = converter.convert(response);
        note.saveAiResult(result.getSummary(), result.getFeedback());
        return StudyNoteResponse.from(note);
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private Schedule findSchedule(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다."));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
