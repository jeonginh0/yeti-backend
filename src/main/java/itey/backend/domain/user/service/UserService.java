package itey.backend.domain.user.service;

import itey.backend.domain.user.dto.UserSearchResponse;
import itey.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserSearchResponse> search(String q, UUID currentUserId) {
        if (q == null || q.strip().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어는 2자 이상 입력해주세요.");
        }
        return userRepository.searchByUsernameOrNickname(q.strip(), currentUserId)
                .stream()
                .map(UserSearchResponse::from)
                .toList();
    }
}
