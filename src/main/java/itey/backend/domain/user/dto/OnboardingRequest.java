package itey.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {
    private String username;  // @멘션용 고유 식별자 (영문/숫자/언더스코어)
    private String nickname;  // 표시 이름 (선택, 미입력 시 OAuth 이름 유지)
}
