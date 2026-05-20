package itey.backend.domain.schedule.service;

import itey.backend.domain.schedule.dto.*;
import itey.backend.domain.schedule.entity.Schedule;
import itey.backend.domain.schedule.entity.ScheduleParticipant;
import itey.backend.domain.schedule.entity.enums.ParticipantStatus;
import itey.backend.domain.schedule.entity.enums.VisibilityType;
import itey.backend.domain.schedule.repository.ScheduleParticipantRepository;
import itey.backend.domain.schedule.repository.ScheduleRepository;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.entity.enums.FriendshipStatus;
import itey.backend.domain.user.repository.FriendshipRepository;
import itey.backend.domain.user.repository.UserRepository;
import itey.backend.global.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FcmService fcmService;

    public ScheduleDetailResponse createSchedule(UUID ownerId, ScheduleCreateRequest req) {
        User owner = findUser(ownerId);

        Schedule schedule = Schedule.builder()
                .owner(owner)
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .allDay(req.isAllDay())
                .location(req.getLocation())
                .recurring(req.isRecurring())
                .recurrenceRule(req.getRecurrenceRule())
                .visibility(req.getVisibility() != null ? req.getVisibility() : VisibilityType.FRIENDS)
                .build();

        scheduleRepository.save(schedule);

        List<ScheduleParticipant> participants = List.of();
        if (req.getParticipantUsernames() != null && !req.getParticipantUsernames().isEmpty()) {
            participants = req.getParticipantUsernames().stream()
                    .filter(username -> !username.equals(owner.getUsername()))
                    .map(username -> {
                        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "사용자를 찾을 수 없습니다: " + username));
                        boolean areFriends = friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(
                                ownerId, user.getId(), FriendshipStatus.ACCEPTED)
                                || friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(
                                user.getId(), ownerId, FriendshipStatus.ACCEPTED);
                        if (!areFriends) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    username + "님은 친구가 아닙니다.");
                        }
                        return ScheduleParticipant.builder()
                                .schedule(schedule)
                                .user(user)
                                .build();
                    })
                    .toList();
            participantRepository.saveAll(participants);

            List<String> tokens = participants.stream()
                    .map(p -> p.getUser().getFcmToken())
                    .filter(t -> t != null && !t.isBlank())
                    .toList();
            if (!tokens.isEmpty()) {
                fcmService.sendToTokens(tokens,
                        "공동 일정 초대",
                        owner.getNickname() + "님이 '" + schedule.getTitle() + "'에 초대했습니다.",
                        Map.of("type", "SCHEDULE_INVITE", "scheduleId", schedule.getId().toString()));
            }
        }

        return ScheduleDetailResponse.from(schedule, participants);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(UUID userId, LocalDateTime from, LocalDateTime to, String category) {
        return scheduleRepository.findMySchedules(userId, from, to, category).stream()
                .map(s -> ScheduleResponse.from(s, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleDetailResponse getSchedule(UUID scheduleId, UUID userId) {
        Schedule schedule = findSchedule(scheduleId);
        checkAccess(schedule, userId);
        List<ScheduleParticipant> participants = participantRepository.findByScheduleId(scheduleId);
        return ScheduleDetailResponse.from(schedule, participants);
    }

    public ScheduleDetailResponse updateSchedule(UUID scheduleId, UUID userId, ScheduleUpdateRequest req) {
        Schedule schedule = findSchedule(scheduleId);
        checkOwner(schedule, userId);

        schedule.update(
                req.getTitle(), req.getDescription(), req.getCategory(),
                req.getStartAt(), req.getEndAt(), req.isAllDay(),
                req.getLocation(), req.isRecurring(), req.getRecurrenceRule(),
                req.getVisibility() != null ? req.getVisibility() : schedule.getVisibility()
        );

        List<ScheduleParticipant> participants = participantRepository.findByScheduleId(scheduleId);
        return ScheduleDetailResponse.from(schedule, participants);
    }

    public void deleteSchedule(UUID scheduleId, UUID userId) {
        Schedule schedule = findSchedule(scheduleId);
        checkOwner(schedule, userId);
        participantRepository.deleteByScheduleId(scheduleId);
        scheduleRepository.delete(schedule);
    }

    public void completeSchedule(UUID scheduleId, UUID userId) {
        Schedule schedule = findSchedule(scheduleId);
        checkOwner(schedule, userId);
        schedule.complete();
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getInvitations(UUID userId) {
        return participantRepository.findByUserIdAndStatus(userId, ParticipantStatus.PENDING)
                .stream()
                .map(InvitationResponse::from)
                .toList();
    }

    public void respondToInvitation(UUID scheduleId, UUID userId, String action) {
        ScheduleParticipant participant = participantRepository
                .findByScheduleIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "초대 내역을 찾을 수 없습니다."));

        if (participant.getStatus() != ParticipantStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 처리된 초대입니다.");
        }

        switch (action.toUpperCase()) {
            case "ACCEPT" -> participant.accept();
            case "REJECT" -> participant.reject();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action은 ACCEPT 또는 REJECT만 가능합니다.");
        }
    }

    public void proposeAdjust(UUID scheduleId, UUID userId, AdjustRequest req) {
        ScheduleParticipant participant = participantRepository
                .findByScheduleIdAndUserId(scheduleId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "초대 내역을 찾을 수 없습니다."));

        participant.requestAdjust(req.getProposedStart(), req.getProposedEnd());
    }

    private Schedule findSchedule(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없습니다."));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private void checkOwner(Schedule schedule, UUID userId) {
        if (!schedule.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "일정 소유자만 수행할 수 있습니다.");
        }
    }

    private void checkAccess(Schedule schedule, UUID userId) {
        boolean isOwner = schedule.getOwner().getId().equals(userId);
        if (isOwner) return;

        boolean isParticipant = participantRepository.findByScheduleIdAndUserId(schedule.getId(), userId).isPresent();
        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
    }
}
