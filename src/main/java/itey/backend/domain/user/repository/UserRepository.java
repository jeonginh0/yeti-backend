package itey.backend.domain.user.repository;

import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.entity.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
