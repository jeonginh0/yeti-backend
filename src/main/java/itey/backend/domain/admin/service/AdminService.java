package itey.backend.domain.admin.service;

import itey.backend.domain.admin.dto.*;
import itey.backend.domain.admin.repository.AdminQueryRepository;
import itey.backend.domain.log.entity.Report;
import itey.backend.domain.log.entity.enums.ReportStatus;
import itey.backend.domain.log.repository.AiParseLogRepository;
import itey.backend.domain.log.repository.ApiLogRepository;
import itey.backend.domain.log.repository.ReportRepository;
import itey.backend.domain.schedule.repository.ScheduleRepository;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.repository.UserRepository;
import itey.backend.global.fcm.FcmService;
import itey.backend.global.mail.AdminMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final String MAINTENANCE_KEY = "system:maintenance";
    private static final int FCM_BATCH_SIZE = 500;

    private final AdminQueryRepository adminQueryRepository;
    private final UserRepository userRepository;
    private final ApiLogRepository apiLogRepository;
    private final AiParseLogRepository aiParseLogRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReportRepository reportRepository;
    private final FcmService fcmService;
    private final AdminMailService adminMailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();

        return new DashboardResponse(
                apiLogRepository.countDistinctUsersSince(monthStart),
                userRepository.countByCreatedAtGreaterThanEqual(dayStart),
                scheduleRepository.countByCreatedAtGreaterThanEqual(dayStart),
                aiParseLogRepository.sumCostUsdSince(monthStart)
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(String keyword, Boolean active, Pageable pageable) {
        return adminQueryRepository.findUsers(keyword, active, pageable)
                .map(AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse suspendUser(UUID userId) {
        User user = findUser(userId);
        user.deactivate();
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse activateUser(UUID userId) {
        User user = findUser(userId);
        user.activate();
        return AdminUserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<ApiLogResponse> getApiLogs(Pageable pageable) {
        return apiLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ApiLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AiLogResponse> getAiLogs(Pageable pageable) {
        return aiParseLogRepository.findAll(pageable)
                .map(AiLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
        Page<Report> page = (status != null)
                ? reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(ReportResponse::from);
    }

    @Transactional
    public ReportResponse reviewReport(UUID reportId, UUID adminId, ReviewRequest req) {
        if (req.getStatus() == ReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REVIEWED 또는 DISMISSED만 가능합니다.");
        }
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."));
        User admin = findUser(adminId);

        if (req.getStatus() == ReportStatus.DISMISSED) {
            report.dismiss(admin);
        } else {
            report.review(admin);
        }
        return ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public Page<AdminScheduleResponse> getSchedules(UUID ownerId, LocalDateTime from,
                                                    LocalDateTime to, Boolean completed,
                                                    Pageable pageable) {
        return adminQueryRepository.findSchedules(ownerId, from, to, completed, pageable)
                .map(AdminScheduleResponse::from);
    }

    public void sendEmail(EmailRequest req) {
        List<String> emails = userRepository.findAllById(req.getUserIds()).stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .toList();
        if (emails.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효한 이메일 주소를 가진 사용자가 없습니다.");
        }
        adminMailService.sendToUsers(emails, req.getSubject(), req.getBody());
    }

    public void broadcast(BroadcastRequest req) {
        List<String> tokens = userRepository.findAllActiveFcmTokens();
        for (int i = 0; i < tokens.size(); i += FCM_BATCH_SIZE) {
            List<String> batch = tokens.subList(i, Math.min(i + FCM_BATCH_SIZE, tokens.size()));
            fcmService.sendToTokens(batch, req.getTitle(), req.getBody(), Map.of("type", "BROADCAST"));
        }
    }

    public boolean setMaintenanceMode(boolean enabled) {
        redisTemplate.opsForValue().set(MAINTENANCE_KEY, String.valueOf(enabled));
        return enabled;
    }

    public boolean getMaintenanceMode() {
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(MAINTENANCE_KEY));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
