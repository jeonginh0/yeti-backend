package itey.backend.domain.user.service;

import itey.backend.domain.user.dto.FriendRequestDto;
import itey.backend.domain.user.dto.FriendResponse;
import itey.backend.domain.user.entity.Friendship;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.entity.enums.FriendshipStatus;
import itey.backend.domain.user.repository.FriendshipRepository;
import itey.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(UUID userId) {
        return friendshipRepository.findAcceptedFriendships(userId).stream()
                .map(f -> FriendResponse.from(f, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getPendingRequests(UUID userId) {
        return friendshipRepository.findPendingRequests(userId).stream()
                .map(f -> FriendResponse.from(f, userId))
                .toList();
    }

    public FriendResponse sendRequest(UUID requesterId, FriendRequestDto dto) {
        if (requesterId.equals(findUserByUsername(dto.getUsername()).getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        User addressee = findUserByUsername(dto.getUsername());

        friendshipRepository.findBetween(requesterId, addressee.getId()).ifPresent(f -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    switch (f.getStatus()) {
                        case PENDING -> "이미 친구 요청이 존재합니다.";
                        case ACCEPTED -> "이미 친구 관계입니다.";
                        case BLOCKED -> "차단된 사용자입니다.";
                    });
        });

        User requester = findUser(requesterId);
        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);
        return FriendResponse.from(friendship, requesterId);
    }

    public void acceptRequest(UUID friendshipId, UUID userId) {
        Friendship friendship = findFriendship(friendshipId);

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수락 권한이 없습니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대기 중인 요청이 아닙니다.");
        }

        friendship.accept();
    }

    public void rejectRequest(UUID friendshipId, UUID userId) {
        Friendship friendship = findFriendship(friendshipId);

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "거절 권한이 없습니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대기 중인 요청이 아닙니다.");
        }

        friendshipRepository.delete(friendship);
    }

    public void deleteFriend(UUID friendshipId, UUID userId) {
        Friendship friendship = findFriendship(friendshipId);

        boolean isParty = friendship.getRequester().getId().equals(userId)
                || friendship.getAddressee().getId().equals(userId);
        if (!isParty) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "친구 관계가 아닙니다.");
        }

        friendshipRepository.delete(friendship);
    }

    public void blockUser(UUID friendshipId, UUID userId) {
        Friendship friendship = findFriendship(friendshipId);

        boolean isParty = friendship.getRequester().getId().equals(userId)
                || friendship.getAddressee().getId().equals(userId);
        if (!isParty) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        friendship.block();
    }

    private Friendship findFriendship(UUID friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다."));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + username));
    }
}
