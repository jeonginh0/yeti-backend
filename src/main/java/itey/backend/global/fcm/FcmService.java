package itey.backend.global.fcm;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FcmService {

    public void sendToToken(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) return;

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 - token: {}, error: {}", fcmToken, e.getMessage());
        }
    }

    public void sendToTokens(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        List<String> validTokens = fcmTokens.stream()
                .filter(t -> t != null && !t.isBlank())
                .toList();
        if (validTokens.isEmpty()) return;

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(validTokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .setBadge(1)
                                .build())
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            if (response.getFailureCount() > 0) {
                log.warn("FCM 멀티캐스트 일부 실패 - 성공: {}, 실패: {}",
                        response.getSuccessCount(), response.getFailureCount());
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM 멀티캐스트 전송 실패: {}", e.getMessage());
        }
    }
}
