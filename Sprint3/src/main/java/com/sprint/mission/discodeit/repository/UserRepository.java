package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);           // 저장하기
    Optional<User> findById(String id); // ID로 찾기
    List<User> findAll();           // 전체 목록 가져오기
    void delete(String id);         // 삭제하기
}