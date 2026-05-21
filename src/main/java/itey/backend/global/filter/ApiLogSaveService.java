package itey.backend.global.filter;

import itey.backend.domain.log.entity.ApiLog;
import itey.backend.domain.log.repository.ApiLogRepository;
import itey.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiLogSaveService {

    private final ApiLogRepository apiLogRepository;

    @Async
    @Transactional
    public void save(User user, String method, String path, int statusCode, int responseMs, String ipHash, String userAgent) {
        apiLogRepository.save(ApiLog.builder()
                .user(user)
                .method(method)
                .path(path)
                .statusCode(statusCode)
                .responseMs(responseMs)
                .ipHash(ipHash)
                .userAgent(userAgent)
                .build());
    }
}
