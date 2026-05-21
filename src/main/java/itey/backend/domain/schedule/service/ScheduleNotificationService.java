package itey.backend.domain.schedule.service;

import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.schedule.entity.ScheduleParticipant;
import itey.backend.domain.schedule.entity.enums.ParticipantStatus;
import itey.backend.domain.schedule.repository.ScheduleParticipantRepository;
import itey.backend.domain.schedule.repository.ScheduleRepository;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.repository.UserSettingsRepository;
import itey.backend.global.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleNotificationService {

    private static final List<Integer> NOTIFY_INTERVALS = List.of(15, 30, 60, 1440);
    private static final int WINDOW_SECONDS = 25;

    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipantRepository participantRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final FcmService fcmService;

    @Scheduled(fixedDelay = 60_000)
    @Transactional(readOnly = true)
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        for (int notifyBeforeMin : NOTIFY_INTERVALS) {
            LocalDateTime target = now.plusMinutes(notifyBeforeMin);
            List<Schedule> schedules = scheduleRepository.findByStartAtBetween(
                    target.minusSeconds(WINDOW_SECONDS),
                    target.plusSeconds(WINDOW_SECONDS));

            for (Schedule schedule : schedules) {
                notifyIfSettingMatches(schedule.getOwner(), schedule, notifyBeforeMin);

                participantRepository.findByScheduleId(schedule.getId()).stream()
                        .filter(p -> p.getStatus() == ParticipantStatus.ACCEPTED)
                        .map(ScheduleParticipant::getUser)
                        .forEach(user -> notifyIfSettingMatches(user, schedule, notifyBeforeMin));
            }
        }
    }

    private void notifyIfSettingMatches(User user, Schedule schedule, int notifyBeforeMin) {
        userSettingsRepository.findByUserId(user.getId())
                .filter(s -> Objects.equals(s.getNotifyBeforeMin(), notifyBeforeMin))
                .ifPresent(s -> {
                    String token = user.getFcmToken();
                    if (token != null && !token.isBlank()) {
                        fcmService.sendToToken(token,
                                "일정 알림",
                                "'" + schedule.getTitle() + "' 일정이 " + formatMinutes(notifyBeforeMin) + " 시작됩니다.",
                                Map.of("type", "SCHEDULE_REMINDER", "scheduleId", schedule.getId().toString()));
                        log.info("일정 알림 발송 - userId: {}, scheduleId: {}", user.getId(), schedule.getId());
                    }
                });
    }

    private String formatMinutes(int minutes) {
        return switch (minutes) {
            case 15 -> "15분 후에";
            case 30 -> "30분 후에";
            case 60 -> "1시간 후에";
            case 1440 -> "내일";
            default -> minutes + "분 후에";
        };
    }
}
