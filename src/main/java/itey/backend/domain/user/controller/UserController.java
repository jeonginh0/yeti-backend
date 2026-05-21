package itey.backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itey.backend.domain.user.dto.UserSearchResponse;
import itey.backend.domain.user.dto.UserSettingsResponse;
import itey.backend.domain.user.dto.UserSettingsUpdateRequest;
import itey.backend.domain.user.service.UserService;
import itey.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 검색 (username 또는 닉네임, 최대 20명)")
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> search(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String q) {
        return ResponseEntity.ok(userService.search(q, principal.getId()));
    }

    @Operation(summary = "내 설정 조회")
    @GetMapping("/settings")
    public ResponseEntity<UserSettingsResponse> getSettings(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getSettings(principal.getId()));
    }

    @Operation(summary = "내 설정 수정 (알림 시간: 15/30/60/1440분)")
    @PatchMapping("/settings")
    public ResponseEntity<UserSettingsResponse> updateSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserSettingsUpdateRequest request) {
        return ResponseEntity.ok(userService.updateSettings(principal.getId(), request));
    }
}
