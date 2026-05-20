package itey.backend.domain.chat.service;

import itey.backend.domain.chat.dto.MediaUploadResponse;
import itey.backend.global.config.SupabaseStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final SupabaseStorageProperties properties;
    private final RestClient restClient = RestClient.create();

    public MediaUploadResponse createUploadUrl(UUID roomId, String contentType) {
        String ext = resolveExtension(contentType);
        String objectPath = "chat/" + roomId + "/" + UUID.randomUUID() + "." + ext;
        String bucket = properties.getStorage().getBucket();
        String supabaseUrl = properties.getUrl();

        String signApiUrl = supabaseUrl + "/storage/v1/object/sign/upload/" + bucket + "/" + objectPath;

        Map<?, ?> response = restClient.post()
                .uri(signApiUrl)
                .header("Authorization", "Bearer " + properties.getServiceRoleKey())
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), (req, res) -> {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "스토리지 업로드 URL 생성 실패");
                })
                .body(Map.class);

        if (response == null || !response.containsKey("url")) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "스토리지 업로드 URL 생성 실패");
        }

        String token = (String) response.get("url");
        String uploadUrl = supabaseUrl + "/storage/v1" + token;
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;

        return new MediaUploadResponse(uploadUrl, publicUrl, objectPath);
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            case "video/quicktime" -> "mov";
            case "application/pdf" -> "pdf";
            default -> "bin";
        };
    }
}
