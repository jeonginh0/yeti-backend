package itey.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailSignupRequest {
    private String email;
    private String password;
    private String username;  // @멘션용 고유 식별자 (필수)
    private String nickname;  // 표시 이름 (선택, 미입력 시 이메일 앞부분 사용)
}
