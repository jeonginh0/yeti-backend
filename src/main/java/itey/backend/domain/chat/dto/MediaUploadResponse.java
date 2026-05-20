package itey.backend.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponse {

    private String uploadUrl;
    private String publicUrl;
    private String path;
}
