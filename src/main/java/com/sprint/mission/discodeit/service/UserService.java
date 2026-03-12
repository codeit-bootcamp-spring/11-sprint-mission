package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Domain.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UUID create(User user);

    // 단건 조회
    User read(UUID id);

    // 전체 조회
    List<User> readAll();

    // 수정
    void update(UUID id, String userName, String userNickname);

    // 삭제
    void delete(UUID id);
}
