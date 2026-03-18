package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUserService implements UserService {

    // 메모리에 User를 저장할 Map
    private final Map<UUID, User> data;

    // 생성자에서 Map 초기화
    public JCFUserService() {
        this.data = new HashMap<>();
    }

    // 생성
    @Override
    public User create(User user) {
        data.put(user.getId(), user);
        return user;
    }

    // 단건 조회
    @Override
    public User getById(UUID id) {
        return data.get(id);   // 없으면 null 반환
    }

    // 전체 조회
    @Override
    public List<User> getAll() {
        return new ArrayList<>(data.values());
    }

    // 수정
    @Override
    public User update(User user) {
        data.put(user.getId(), user);
        return user;
    }

    // 삭제
    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
