package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // 유저 생성
    User create(User user);

    // id로 한 명 조회
    User getById(UUID id);

    // 모든 유저 조회
    List<User> getAll();

    // 유저 정보 수정
    User update(User user);

    // 유저 삭제
    void delete(UUID id);
}
