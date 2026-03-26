package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserDto login(LoginRequest request){
        User user = userRepository.findByUserName(request.userName());

        if(user == null){
            throw new IllegalArgumentException("일치하는 유저가 없습니다.");
        }

        if(!user.getPassword().equals(request.password())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId());

        boolean online = userStatus != null && userStatus.isOnline();

        return new UserDto(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getStatusMessage(),
                user.getProfileId(),
                online,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
