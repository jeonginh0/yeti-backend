package itey.backend.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itey.backend.domain.admin.dto.*;
import itey.backend.domain.admin.service.AdminService;
import itey.backend.domain.log.entity.enums.ReportStatus;
import itey.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Admin", description = "어드민 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "대시보드 통계 조회")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @Operation(summary = "사용자 목록 조회")
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getUsers(keyword, active, pageable));
    }

    @Operation(summary = "사용자 정지")
    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<AdminUserResponse> suspendUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.suspendUser(id));
    }

    @Operation(summary = "사용자 활성화")
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<AdminUserResponse> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.activateUser(id));
    }

    @Operation(summary = "API 로그 조회")
    @GetMapping("/logs/api")
    public ResponseEntity<Page<ApiLogResponse>> getApiLogs(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getApiLogs(pageable));
    }

    @Operation(summary = "AI 파싱 로그 조회")
    @GetMapping("/logs/ai")
    public ResponseEntity<Page<AiLogResponse>> getAiLogs(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAiLogs(pageable));
    }

    @Operation(summary = "일정 목록 조회")
    @GetMapping("/schedules")
    public ResponseEntity<Page<AdminScheduleResponse>> getSchedules(
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Boolean completed,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getSchedules(ownerId, from, to, completed, pageable));
    }

    @Operation(summary = "신고 목록 조회")
    @GetMapping("/reports")
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getReports(status, pageable));
    }

    @Operation(summary = "신고 처리 (REVIEWED / DISMISSED)")
    @PatchMapping("/reports/{id}/review")
    public ResponseEntity<ReportResponse> reviewReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(adminService.reviewReport(id, principal.getId(), request));
    }

    @Operation(summary = "이메일 발송 (단일/다수 사용자)")
    @PostMapping("/notifications/email")
    public ResponseEntity<Void> sendEmail(@RequestBody @Valid EmailRequest request) {
        adminService.sendEmail(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 공지 푸시 발송")
    @PostMapping("/notifications/broadcast")
    public ResponseEntity<Void> broadcast(@RequestBody BroadcastRequest request) {
        adminService.broadcast(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "점검 모드 설정")
    @PostMapping("/system/maintenance")
    public ResponseEntity<Map<String, Boolean>> setMaintenance(@RequestParam boolean enabled) {
        return ResponseEntity.ok(Map.of("maintenance", adminService.setMaintenanceMode(enabled)));
    }

    @Operation(summary = "점검 모드 상태 조회")
    @GetMapping("/system/maintenance")
    public ResponseEntity<Map<String, Boolean>> getMaintenance() {
        return ResponseEntity.ok(Map.of("maintenance", adminService.getMaintenanceMode()));
    }
}
