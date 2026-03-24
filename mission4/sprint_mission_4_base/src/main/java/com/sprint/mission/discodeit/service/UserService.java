package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    // 회원 가입 및 유저 생성
    User create(UserCreateRequest userCreateRequest, Optional<BinaryContentCreateRequest> profileCreateRequest);
    UserDto find(UUID userId);
    List<UserDto> findAll();
    User update(UUID userId, UserUpdateRequest userUpdateRequest, Optional<BinaryContentCreateRequest> profileCreateRequest);
    void delete(UUID userId);
}

private UserDto convertToDto(User user, UserStatus userStatus) {
    boolean isOnline = Duration.between(userStatus.getLastSeen(), Instant.now()).toMinutes() < 5;

    // Builder 대신 Record 생성자 호출
    return new UserDto(
            user.getId(),
            user.getCreatedAt(),
            user.getUpdatedAt(), // User는 가변 도메인이므로 포함
            user.getUsername(),
            user.getEmail(),
            null, // profileId (추후 구현 시 할당)
            isOnline
    );
}