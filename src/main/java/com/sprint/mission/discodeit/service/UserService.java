package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

  UserDto create(UserCreateRequest dto,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest);

  List<UserDto> findAll();

  UserDto findById(UUID id);

  UserDto update(UUID id, UserUpdateRequest dto,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest);

  void delete(UUID id);
}
