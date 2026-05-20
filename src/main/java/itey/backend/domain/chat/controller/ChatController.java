package itey.backend.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itey.backend.domain.chat.dto.ChatRoomCreateRequest;
import itey.backend.domain.chat.dto.ChatRoomResponse;
import itey.backend.domain.chat.dto.MediaUploadRequest;
import itey.backend.domain.chat.dto.MediaUploadResponse;
import itey.backend.domain.chat.dto.MessageResponse;
import itey.backend.domain.chat.dto.ReactRequest;
import itey.backend.domain.chat.service.ChatService;
import itey.backend.domain.chat.service.SupabaseStorageService;
import itey.backend.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Chat", description = "채팅 API")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;
    private final SupabaseStorageService supabaseStorageService;

    @Operation(summary = "내 채팅방 목록 조회")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(chatService.getMyRooms(principal.getId()));
    }

    @Operation(summary = "채팅방 생성 (DM / 그룹)")
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChatRoomCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createRoom(principal.getId(), request));
    }

    @Operation(summary = "채팅방 메시지 목록 조회 (커서 페이징)")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId,
            @RequestParam(required = false) UUID before,
            @RequestParam(defaultValue = "50") int size) {
        chatService.updateLastRead(roomId, principal.getId());
        return ResponseEntity.ok(chatService.getMessages(roomId, principal.getId(), before, size));
    }

    @Operation(summary = "미디어 업로드 presigned URL 발급")
    @PostMapping("/rooms/{roomId}/media")
    public ResponseEntity<MediaUploadResponse> getMediaUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId,
            @Valid @RequestBody MediaUploadRequest request) {
        return ResponseEntity.ok(supabaseStorageService.createUploadUrl(roomId, request.getContentType()));
    }

    @Operation(summary = "메시지 리액션 토글")
    @PatchMapping("/messages/{messageId}/react")
    public ResponseEntity<MessageResponse> react(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID messageId,
            @Valid @RequestBody ReactRequest request) {
        return ResponseEntity.ok(chatService.addReaction(messageId, principal.getId(), request.getEmoji()));
    }

    @Operation(summary = "메시지 삭제 (소프트)")
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID messageId) {
        chatService.deleteMessage(messageId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
