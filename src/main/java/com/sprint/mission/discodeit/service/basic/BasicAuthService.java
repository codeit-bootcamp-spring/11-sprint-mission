package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdatedRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDto updateRole(UserRoleUpdatedRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> UserNotFoundException.withId(request.userId()));
        user.changeRole(request.newRole());
        return userMapper.toDto(user);
    }

}
