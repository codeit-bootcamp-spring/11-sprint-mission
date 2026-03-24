package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserInfoDto create(CreateUserDto createUserDTO);
    UserInfoDto find(UUID userId);
    List<UserDto> findAll();
    UserInfoDto updateUser(UpdateUserDto updateUserDto);
    boolean delete(DeleteUserDto deleteUserDto);







}
