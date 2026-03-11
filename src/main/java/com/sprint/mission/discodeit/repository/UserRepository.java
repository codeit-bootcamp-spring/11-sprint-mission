package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Domain.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {
    UUID create(User user);

    // 단건 조회
    User read(UUID id);

    // 전체 조회
    List<User> readAll();

    // 삭제
    void delete(UUID id);

    // 복구
    void restore(UUID id);
}
