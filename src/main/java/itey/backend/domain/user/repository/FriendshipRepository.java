package itey.backend.domain.user.repository;

import itey.backend.domain.user.entity.Friendship;
import itey.backend.domain.user.entity.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
            AND f.status = 'ACCEPTED'
            """)
    List<Friendship> findAcceptedFriendships(@Param("userId") UUID userId);

    @Query("""
            SELECT f FROM Friendship f
            WHERE f.addressee.id = :userId
            AND f.status = 'PENDING'
            """)
    List<Friendship> findPendingRequests(@Param("userId") UUID userId);

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.requester.id = :userId AND f.addressee.id = :targetId)
            OR (f.requester.id = :targetId AND f.addressee.id = :userId)
            """)
    Optional<Friendship> findBetween(@Param("userId") UUID userId, @Param("targetId") UUID targetId);

    boolean existsByRequesterIdAndAddresseeIdAndStatus(UUID requesterId, UUID addresseeId, FriendshipStatus status);
}
