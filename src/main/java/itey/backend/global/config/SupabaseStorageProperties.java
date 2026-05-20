package itey.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "supabase")
public class SupabaseStorageProperties {

    private String url;
    private String serviceRoleKey;
    private Storage storage = new Storage();

    @Getter
    @Setter
    public static class Storage {
        private String bucket;
    }
}
