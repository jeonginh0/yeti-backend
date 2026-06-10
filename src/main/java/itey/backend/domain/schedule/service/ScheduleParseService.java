package itey.backend.domain.schedule.service;

import itey.backend.domain.log.entity.AiParseLog;
import itey.backend.domain.log.repository.AiParseLogRepository;
import itey.backend.domain.schedule.dto.ParseResponse;
import itey.backend.domain.schedule.dto.ParsedScheduleResult;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleParseService {

    private static final int DAILY_LIMIT = 50;
    private static final BigDecimal INPUT_COST_PER_TOKEN = new BigDecimal("0.00000015");
    private static final BigDecimal OUTPUT_COST_PER_TOKEN = new BigDecimal("0.0000006");

    private final ChatClient chatClient;
    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final StringRedisTemplate redisTemplate;
    private final AiParseLogRepository aiParseLogRepository;
    private final UserRepository userRepository;

    public ParseResponse parse(UUID userId, String input) {
        User user = prepareUser(userId);
        return doParse(user, input, null);
    }

    public ParseResponse parseVoice(UUID userId, MultipartFile audio) {
        User user = prepareUser(userId);
        String transcribedText = transcribe(audio);
        if (transcribedText == null || transcribedText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음성에서 텍스트를 인식하지 못했습니다.");
        }
        return doParse(user, transcribedText, transcribedText);
    }

    private User prepareUser(UUID userId) {
        checkDailyLimit(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private String transcribe(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음성 파일이 비어 있습니다.");
        }
        try {
            String filename = audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.m4a";
            Resource resource = new ByteArrayResource(audio.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                    .language("ko")
                    .responseFormat(TranscriptResponseFormat.TEXT)
                    .temperature(0f)
                    .build();
            AudioTranscriptionResponse response =
                    transcriptionModel.call(new AudioTranscriptionPrompt(resource, options));
            return response.getResult().getOutput();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "음성 파일을 읽을 수 없습니다.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "음성 인식에 실패했습니다.");
        }
    }

    private ParseResponse doParse(User user, String input, String transcribedText) {
        BeanOutputConverter<ParsedScheduleResult> converter =
                new BeanOutputConverter<>(ParsedScheduleResult.class);

        String systemPrompt = buildSystemPrompt();
        String userMessage = input + "\n\n" + converter.getFormat();

        try {
            ChatResponse chatResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .chatResponse();

            String content = chatResponse.getResult().getOutput().getText();
            ParsedScheduleResult result = converter.convert(content);

            int promptTokens = chatResponse.getMetadata().getUsage().getPromptTokens();
            int completionTokens = chatResponse.getMetadata().getUsage().getCompletionTokens();
            BigDecimal cost = INPUT_COST_PER_TOKEN.multiply(BigDecimal.valueOf(promptTokens))
                    .add(OUTPUT_COST_PER_TOKEN.multiply(BigDecimal.valueOf(completionTokens)));

            aiParseLogRepository.save(AiParseLog.builder()
                    .user(user)
                    .rawInput(input)
                    .parsedResult(content)
                    .confidence(result.getAiConfidence())
                    .tokensUsed(promptTokens + completionTokens)
                    .costUsd(cost)
                    .planStatus(user.getPlanStatus().name())
                    .success(true)
                    .build());

            incrementDailyCounter(user.getId());

            return toParseResponse(result, transcribedText);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            aiParseLogRepository.save(AiParseLog.builder()
                    .user(user)
                    .rawInput(input)
                    .planStatus(user.getPlanStatus().name())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 파싱에 실패했습니다.");
        }
    }

    private void checkDailyLimit(UUID userId) {
        String key = dailyKey(userId);
        String count = redisTemplate.opsForValue().get(key);
        if (count != null && Integer.parseInt(count) >= DAILY_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "일일 AI 파싱 횟수 한도(" + DAILY_LIMIT + "회)를 초과했습니다.");
        }
    }

    private void incrementDailyCounter(UUID userId) {
        String key = dailyKey(userId);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofDays(1));
    }

    private String dailyKey(UUID userId) {
        return "ai:parse:" + userId + ":" + LocalDate.now();
    }

    private String buildSystemPrompt() {
        return """
                당신은 한국어 자연어 일정 파싱 전문가입니다.
                현재 날짜/시간: %s

                사용자 입력에서 일정 정보를 추출하세요. 규칙:
                - category: 반드시 학습, 운동, 약속, 업무, 기타 중 하나
                - startAt, endAt: ISO-8601 형식 (yyyy-MM-ddTHH:mm:ss), 시간이 불명확하면 null
                - 상대적 날짜(내일, 모레, 다음주 등)는 현재 날짜 기준 절대 날짜로 변환
                - participants: @태그된 username 목록 (@ 기호 제외), 없으면 빈 배열
                - recurring: 반복 일정 여부 (매일/매주/매월 등 반복 표현이 있으면 true)
                - recurrenceRule: recurring이 true일 때만 iCal RRULE 형식으로 작성 (예: FREQ=WEEKLY;BYDAY=MO, FREQ=DAILY, FREQ=MONTHLY;BYMONTHDAY=1), 없으면 null
                - aiConfidence: 파싱 정확도 확신도 (0.0~1.0), 정보가 불명확하거나 누락될수록 낮게 설정
                """.formatted(LocalDateTime.now());
    }

    private ParseResponse toParseResponse(ParsedScheduleResult result, String transcribedText) {
        LocalDateTime startAt = parseDateTime(result.getStartAt());
        LocalDateTime endAt = parseDateTime(result.getEndAt());
        float confidence = result.getAiConfidence() != null ? result.getAiConfidence() : 0f;
        boolean recurring = Boolean.TRUE.equals(result.getRecurring());

        return new ParseResponse(
                result.getTitle(),
                result.getCategory(),
                startAt,
                endAt,
                result.getParticipants() != null ? result.getParticipants() : List.of(),
                result.getLocation(),
                recurring,
                recurring ? result.getRecurrenceRule() : null,
                confidence,
                confidence < 0.7f,
                transcribedText
        );
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
