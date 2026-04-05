package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.*;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDto> findAllUserDtos();
    User createUser(UserCreateRequest request, MultipartFile profile);
    User updateUser(UUID userId, UserUpdateRequest request, MultipartFile profile);
    void deleteUser(UUID userId);
}
