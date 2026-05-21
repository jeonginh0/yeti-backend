package itey.backend.domain.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class EmailRequest {

    @NotEmpty
    private List<UUID> userIds;

    @NotBlank
    private String subject;

    @NotBlank
    private String body;
}
