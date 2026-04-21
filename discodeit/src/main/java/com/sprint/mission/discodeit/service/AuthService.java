package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import com.sprint.mission.discodeit.service.dto.user.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto login(UserLoginRequest request) {
        validateLoginRequest(request);
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new DiscodeitException(ErrorCode.LOGIN_USER_NOT_FOUND));
        if (!user.getPassword().equals(request.password())) {
            throw new DiscodeitException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 기존 UserStatus가 있으면 변경 감지로 업데이트, 없으면 새로 생성
        UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                .orElseGet(() -> userStatusRepository.save(new UserStatus(user)));
        userStatus.updateLastActiveAt();

        return userMapper.toDto(user);
    }

    private void validateLoginRequest(UserLoginRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.LOGIN_REQUEST_REQUIRED);
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new DiscodeitException(ErrorCode.USERNAME_REQUIRED);
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new DiscodeitException(ErrorCode.PASSWORD_REQUIRED);
        }
    }
}
