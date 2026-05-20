package itey.backend.domain.user.dto;

import itey.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {

    private UUID id;
    private String username;
    private String nickname;
    private String profileImageUrl;
    private String statusMessage;

    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getStatusMessage()
        );
    }
}
