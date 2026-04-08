package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JCFUserRepository implements UserRepository {
    private final List<User> database = new ArrayList<>();

    @Override
    public void save(User user) {
        database.add(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return database.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(database);
    }

    @Override
    public void delete(UUID id) {
        database.removeIf(u -> u.getId().equals(id));
    }
}