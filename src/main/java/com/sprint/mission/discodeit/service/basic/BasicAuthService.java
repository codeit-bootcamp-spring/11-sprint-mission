package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;

    @Override
    public UserDto login(LoginRequest request) {
        User user = userRepository.findByName(request.name()).
                orElseThrow(() -> new IllegalArgumentException("일치하는 유저가 없습니다."));
        if (!user.getPassword().equals(request.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return new UserDto(user.getId(), user.getName(), user.getEmail(),
                user.getProfileId(), user.getCreatedAt(), user.getUpdatedAt(),
                true);
    }
}
