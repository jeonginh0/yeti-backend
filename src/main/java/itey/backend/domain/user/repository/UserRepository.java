package itey.backend.domain.user.repository;

import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.entity.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            SELECT u FROM User u
            WHERE u.active = true
            AND u.id != :currentUserId
            AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY u.username ASC
            LIMIT 20
            """)
    List<User> searchByUsernameOrNickname(@Param("q") String q, @Param("currentUserId") UUID currentUserId);
}
