package itey.backend.domain.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itey.backend.domain.schedule.dto.*;
import itey.backend.domain.schedule.service.ScheduleParseService;
import itey.backend.domain.schedule.service.ScheduleService;
import itey.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Schedule", description = "일정 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleParseService scheduleParseService;

    @Operation(summary = "받은 일정 초대 목록 (PENDING)")
    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationResponse>> getInvitations(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(scheduleService.getInvitations(principal.getId()));
    }

    @Operation(summary = "자연어 일정 파싱 (AI)")
    @PostMapping("/parse")
    public ResponseEntity<ParseResponse> parseSchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ParseRequest request) {
        return ResponseEntity.ok(scheduleParseService.parse(principal.getId(), request.getInput()));
    }

    @Operation(summary = "일정 생성")
    @PostMapping
    public ResponseEntity<ScheduleDetailResponse> createSchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ScheduleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.createSchedule(principal.getId(), request));
    }

    @Operation(summary = "내 일정 목록 조회")
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(scheduleService.getSchedules(principal.getId(), from, to, category));
    }

    @Operation(summary = "일정 상세 조회")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> getSchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID scheduleId) {
        return ResponseEntity.ok(scheduleService.getSchedule(scheduleId, principal.getId()));
    }

    @Operation(summary = "일정 수정")
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ScheduleDetailResponse> updateSchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, principal.getId(), request));
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID scheduleId) {
        scheduleService.deleteSchedule(scheduleId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "일정 완료 처리")
    @PatchMapping("/{scheduleId}/complete")
    public ResponseEntity<Void> completeSchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID scheduleId) {
        scheduleService.completeSchedule(scheduleId, principal.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "참여 수락/거절")
    @PatchMapping("/{scheduleId}/respond")
    public ResponseEntity<Void> respondToInvitation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody InvitationRespondRequest request) {
        scheduleService.respondToInvitation(scheduleId, principal.getId(), request.getAction());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "시간 조율 제안")
    @PostMapping("/{scheduleId}/adjust")
    public ResponseEntity<Void> proposeAdjust(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody AdjustRequest request) {
        scheduleService.proposeAdjust(scheduleId, principal.getId(), request);
        return ResponseEntity.ok().build();
    }
}
