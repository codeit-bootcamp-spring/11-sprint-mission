package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
// @Repository
public class JCFUserRepository implements UserRepository {

    private final Map<UUID, User> userData = new ConcurrentHashMap<>();

    @Override
    public void save(User user) {
        userData.put(user.getId(), user);
    }

    @Override
    public User findById(UUID id) {
        User user = userData.get(id);
        if (user != null && user.isDeleted()) {
            return null;
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        if (userData.isEmpty()) {
            return new ArrayList<>();
        }
        return userData.values().stream()
                .filter(user -> !user.isDeleted())
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        User user = findById(id);
        if (user != null) {
            user.softDelete();
            save(user);
        }
    }
}