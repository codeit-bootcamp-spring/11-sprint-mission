package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {
    List<User> users;

    public JCFUserRepository() {
        init();
    }

    @Override
    public void init() {
        users = new ArrayList<>();
    }

    @Override
    public void save(User user) {
        users.add(user);
    }

    @Override
    public User load(UUID id) {
        List<User> list;
        list = users.stream()
                .filter(p -> p.getId().equals(id))
                .toList();
        if(list.isEmpty()) {
            throw new IllegalArgumentException("User Not Found: " + id);
        }
        return list.get(0);
    }

    @Override
    public List<User> loadAll() {
        return users;
    }

    @Override
    public void delete(User user) {
        users.remove(user);
    }
}
