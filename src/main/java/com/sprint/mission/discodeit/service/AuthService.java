package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserInfoDto;
import com.sprint.mission.discodeit.dto.UserLoginDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    public UserInfoDto login(UserLoginDto dto) {
        // dto에서 가져온 이름으로 찾은 유저가 있다면, 유저 비밀번호와 일치하는지 확인
        User user = userRepository.findByName(dto.name())
                .orElseThrow(() -> new IllegalArgumentException("없는 user id 입니다."));

        if (!user.getPassword().equals(dto.password())) {
            throw new IllegalArgumentException("잘못된 비밀번호 입니다.");
        }

        UserStatus status = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("없는 userStatus id 입니다."));

        return new UserInfoDto(user.getId(), user.getName(), user.getEmail(), user.getProfileImageId(), status.isOnline());
    }
}
