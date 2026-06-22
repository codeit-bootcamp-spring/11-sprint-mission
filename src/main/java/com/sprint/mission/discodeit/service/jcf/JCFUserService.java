package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class JCFUserService implements UserService {
    private final Map<UUID, User> data;

    public JCFUserService() {
        this.data = new HashMap<>();
    }

    @Override
    public User create(String username, String email, String password) {
        User user = new User(username, email, password);
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public User find(UUID userId) {
        User userNullable = this.data.get(userId);
        return Optional.ofNullable(userNullable)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    @Override
    public List<User> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public User update(UUID userId, String newUsername, String newEmail, String newPassword) {
        User user = find(userId);
        user.update(newUsername, newEmail, newPassword);
        return user;
    }

    @Override
    public void delete(UUID userId) {
        if (!data.containsKey(userId)) {
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        data.remove(userId);
    }
}
