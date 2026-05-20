package itey.backend.domain.chat.dto;

import itey.backend.domain.chat.entity.enums.ChatRoomType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {

    @NotNull
    private ChatRoomType type;

    private String name;

    @NotNull
    private List<String> memberUsernames;

    private UUID pinnedScheduleId;
}
