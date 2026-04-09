package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreateRequest dto, MultipartFile profile);
    UserDto findById(UUID id);
    List<UserDto> findAll();
    void update(UUID id, UserUpdateRequest dto, MultipartFile profile);
    void delete(UUID id);
}
