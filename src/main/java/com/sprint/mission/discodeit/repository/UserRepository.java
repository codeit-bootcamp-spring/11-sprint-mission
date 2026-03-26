package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository {
    User save(User user);

    User findById(UUID id);

    List<User> findAll();

    // User update(User user);
    // >> 레포지토리는 save 하나로 저장,업데이트 다 할수 있으니까

    void delete(UUID id);

    User findByUserName(String userName);
    User findByEmail(String email);
}
