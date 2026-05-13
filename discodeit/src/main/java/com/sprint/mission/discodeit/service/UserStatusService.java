package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.dto.userstatus.CreateUserStatusRequest;
import com.sprint.mission.discodeit.service.dto.userstatus.UpdateUserStatusByUserIdRequest;
import com.sprint.mission.discodeit.service.dto.userstatus.UpdateUserStatusRequest;
import com.sprint.mission.discodeit.service.dto.userstatus.UserStatusDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;

    @Transactional
    public UserStatusDto create(CreateUserStatusRequest request) {
        validateCreateRequest(request);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        if (userStatusRepository.findByUserId(request.userId()).isPresent()) {
            throw new DiscodeitException(ErrorCode.DUPLICATE_USER_STATUS);
        }

        UserStatus userStatus = new UserStatus(user);
        return toDto(userStatusRepository.save(userStatus));
    }

    public UserStatusDto find(UUID id) {
        return toDto(getUserStatus(id));
    }

    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UserStatusDto update(UpdateUserStatusRequest request) {
        validateUpdateRequest(request);
        UserStatus userStatus = getUserStatus(request.userStatusId());
        userStatus.updateLastActiveAt(request.lastActiveAt());
        return toDto(userStatus);
    }

    @Transactional
    public UserStatusDto updateByUserId(UpdateUserStatusByUserIdRequest request) {
        validateUpdateByUserIdRequest(request);
        userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        UserStatus userStatus = userStatusRepository.findByUserId(request.userId())
                .orElseThrow(() -> new DiscodeitException(ErrorCode.USER_STATUS_NOT_FOUND));
        userStatus.updateLastActiveAt(request.lastActiveAt());
        return toDto(userStatus);
    }

    @Transactional
    public UserStatusDto updateByUserId(UUID userId, Instant lastActiveAt) {
        Instant resolved = lastActiveAt != null ? lastActiveAt : Instant.now();
        return updateByUserId(new UpdateUserStatusByUserIdRequest(userId, resolved));
    }

    @Transactional
    public void delete(UUID id) {
        UserStatus userStatus = getUserStatus(id);
        userStatusRepository.delete(userStatus);
    }

    private UserStatus getUserStatus(UUID id) {
        if (id == null) {
            throw new DiscodeitException(ErrorCode.USER_STATUS_ID_REQUIRED);
        }
        return userStatusRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ErrorCode.USER_STATUS_NOT_FOUND));
    }

    private UserStatusDto toDto(UserStatus userStatus) {
        return userStatusMapper.toDto(userStatus);
    }

    private void validateCreateRequest(CreateUserStatusRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "유저상태 생성 요청값이 비어있어요.");
        }
        if (request.userId() == null) {
            throw new DiscodeitException(ErrorCode.USER_ID_REQUIRED);
        }
    }

    private void validateUpdateRequest(UpdateUserStatusRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "유저상태 수정 요청값이 비어있어요.");
        }
        if (request.userStatusId() == null) {
            throw new DiscodeitException(ErrorCode.USER_STATUS_ID_REQUIRED);
        }
        if (request.lastActiveAt() == null) {
            throw new DiscodeitException(ErrorCode.LAST_CONNECTED_AT_REQUIRED);
        }
    }

    private void validateUpdateByUserIdRequest(UpdateUserStatusByUserIdRequest request) {
        if (request == null) {
            throw new DiscodeitException(ErrorCode.INVALID_REQUEST, "유저상태 수정 요청값이 비어있어요.");
        }
        if (request.userId() == null) {
            throw new DiscodeitException(ErrorCode.USER_ID_REQUIRED);
        }
        if (request.lastActiveAt() == null) {
            throw new DiscodeitException(ErrorCode.LAST_CONNECTED_AT_REQUIRED);
        }
    }
}
