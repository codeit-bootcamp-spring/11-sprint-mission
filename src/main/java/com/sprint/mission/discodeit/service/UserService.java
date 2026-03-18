package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.user.CreateUserRequest;
import com.sprint.mission.discodeit.dto.request.user.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
}