/*
package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
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
    public User create(String username, String nickname, String description, String email, String password, UUID profileImageId) {
        User user = new User(username, nickname, description, email, password, profileImageId);
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return Optional.ofNullable(data.get(id))
                .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public User update(UUID id, String username, String nickname, String description, String email, String password, UUID profileImageId) {
        User user = findById(id);
        user.update(username, nickname, description, email, password);
        user.updateProfileImage(profileImageId);
        return user;
    }

    @Override
    public void delete(UUID id) {
        User user = findById(id);
        data.remove(id);
    }
}*/
