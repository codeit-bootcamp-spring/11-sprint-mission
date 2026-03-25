package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정, matchIfMissing : 설정이 안되있으면 jcf
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFUserRepository implements UserRepository {
    private final Map<UUID, User> users = new HashMap<>(); // 저장소

    @Override
    public void insert(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public User findById(UUID id) {
        // containsKey(해당 키가 있는지 조회)와 get(조회)으로 2번 조회(비효율적)
//        if (!users.containsKey(id)) {
//            throw new NoSuchElementException("해당 유저는 존재하지 않습니다. id : " + id);
//        }
//        return users.get(id);

        // get으로 한번에 조회
        User user = users.get(id);
        if (user == null) {
            throw new NoSuchElementException("해당 유저는 존재하지 않습니다. id : " + id);
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return this.users.values().stream().toList();
    }

    @Override
    public void update(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void delete(UUID id) {
        users.remove(id);
    }
}
