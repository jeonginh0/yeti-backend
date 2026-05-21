package itey.backend.domain.admin.dto;

import itey.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private UUID id;
    private String username;
    private String nickname;
    private String email;
    private String provider;
    private String planStatus;
    private boolean active;
    private boolean admin;
    private LocalDateTime createdAt;

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getProvider() != null ? user.getProvider().name() : null,
                user.getPlanStatus() != null ? user.getPlanStatus().name() : null,
                user.isActive(),
                user.isAdmin(),
                user.getCreatedAt()
        );
    }
}
