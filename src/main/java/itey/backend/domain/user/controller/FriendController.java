package itey.backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itey.backend.domain.user.dto.FriendRequestDto;
import itey.backend.domain.user.dto.FriendResponse;
import itey.backend.domain.user.service.FriendService;
import itey.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Friend", description = "친구 API")
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 목록 조회")
    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(friendService.getFriends(principal.getId()));
    }

    @Operation(summary = "받은 친구 요청 목록")
    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponse>> getPendingRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(friendService.getPendingRequests(principal.getId()));
    }

    @Operation(summary = "친구 요청 보내기")
    @PostMapping("/request")
    public ResponseEntity<FriendResponse> sendRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FriendRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendService.sendRequest(principal.getId(), request));
    }

    @Operation(summary = "친구 요청 수락")
    @PatchMapping("/{friendshipId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID friendshipId) {
        friendService.acceptRequest(friendshipId, principal.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "친구 요청 거절")
    @PatchMapping("/{friendshipId}/reject")
    public ResponseEntity<Void> rejectRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID friendshipId) {
        friendService.rejectRequest(friendshipId, principal.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<Void> deleteFriend(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID friendshipId) {
        friendService.deleteFriend(friendshipId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 차단")
    @PostMapping("/{friendshipId}/block")
    public ResponseEntity<Void> blockUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID friendshipId) {
        friendService.blockUser(friendshipId, principal.getId());
        return ResponseEntity.ok().build();
    }
}
