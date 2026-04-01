package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "jcf", matchIfMissing = true)
public class JCFUserRepository implements UserRepository {
    private final Map<UUID, User> data = new HashMap<>();

    @Override
    public void save(User user) {
        data.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<User> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public boolean existsByName(String name) {
        return data.values().stream().anyMatch(user ->
                user.getName().equals(name));
    }

    @Override
    public boolean existsByEmail(String email) {
        return data.values().stream().anyMatch(user ->
                user.getEmail().equals(email));
    }

    @Override
    public Optional<User> findByName(String name) {
        return data.values().stream().filter(user ->
                user.getName().equals(name)).findFirst();
    }
}