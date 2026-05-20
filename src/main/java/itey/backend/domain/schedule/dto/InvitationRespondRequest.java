package itey.backend.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InvitationRespondRequest {

    @NotBlank
    @Pattern(regexp = "ACCEPT|REJECT", message = "action은 ACCEPT 또는 REJECT만 가능합니다.")
    private String action;
}
