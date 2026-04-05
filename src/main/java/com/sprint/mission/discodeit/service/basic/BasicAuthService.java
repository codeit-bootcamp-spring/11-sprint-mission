package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserLoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(UserLoginRequest dto) {
        // dto에서 가져온 이름으로 찾은 유저가 있다면, 유저 비밀번호와 일치하는지 확인
        User user = userRepository.findByName(dto.name())
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));

        if (!user.getPassword().equals(dto.password())) {
            throw new IllegalArgumentException("잘못된 비밀번호 입니다.");
        }

        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        return new UserResponse(user.getId(), user.getCreatedAt(), user.getUpdatedAt(), user.getName(), user.getEmail(), user.getProfileImageId(), status.isOnline());
    }
}
