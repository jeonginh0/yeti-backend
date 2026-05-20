package itey.backend.domain.user.dto;

import itey.backend.domain.user.entity.Friendship;
import itey.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private UUID friendshipId;
    private UUID userId;
    private String username;
    private String nickname;
    private String profileImageUrl;
    private String statusMessage;
    private String status;
    private LocalDateTime createdAt;

    public static FriendResponse from(Friendship friendship, UUID currentUserId) {
        User friend = friendship.getRequester().getId().equals(currentUserId)
                ? friendship.getAddressee()
                : friendship.getRequester();

        return new FriendResponse(
                friendship.getId(),
                friend.getId(),
                friend.getUsername(),
                friend.getNickname(),
                friend.getProfileImageUrl(),
                friend.getStatusMessage(),
                friendship.getStatus().name(),
                friendship.getCreatedAt()
        );
    }
}
