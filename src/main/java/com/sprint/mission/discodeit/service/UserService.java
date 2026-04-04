package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserReadDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    // 코드 탬플릿에 맞게 create 메서드 수정
//    User create(String name, String email, String password);
    User create(UserCreateRequest dto);

    UserReadDto find(UUID id);

    List<UserReadDto> findAll();

    User update(UUID id, UserUpdateRequest dto);

    void delete(UUID id);
}
