package itey.backend.domain.user.service;

import itey.backend.domain.user.dto.UserSearchResponse;
import itey.backend.domain.user.dto.UserSettingsResponse;
import itey.backend.domain.user.dto.UserSettingsUpdateRequest;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.entity.UserSettings;
import itey.backend.domain.user.repository.UserRepository;
import itey.backend.domain.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public List<UserSearchResponse> search(String q, UUID currentUserId) {
        if (q == null || q.strip().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어는 2자 이상 입력해주세요.");
        }
        return userRepository.searchByUsernameOrNickname(q.strip(), currentUserId)
                .stream()
                .map(UserSearchResponse::from)
                .toList();
    }

    @Transactional
    public UserSettingsResponse getSettings(UUID userId) {
        UserSettings settings = findOrCreateSettings(userId);
        return UserSettingsResponse.from(settings);
    }

    @Transactional
    public UserSettingsResponse updateSettings(UUID userId, UserSettingsUpdateRequest req) {
        UserSettings settings = findOrCreateSettings(userId);
        settings.update(
                req.getNotifyBeforeMinAsInt(),
                req.getNotifyOnInvite(),
                req.getNotifyOnChat(),
                req.getTheme(),
                req.getLanguage()
        );
        return UserSettingsResponse.from(settings);
    }

    // 설정 행이 없는 기존/이메일 가입 계정도 자동 복구(self-healing)
    private UserSettings findOrCreateSettings(UUID userId) {
        return userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
                    return userSettingsRepository.save(UserSettings.builder().user(user).build());
                });
    }
}
