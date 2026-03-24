package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFUserService implements UserService {
    private final List<User> data;

    public JCFUserService() {
        this.data = new ArrayList<>();
    }

    @Override
    public void create(User user) {
        data.add(user);
    }

    @Override
    public User findById(UUID id) {
        return data.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data);
    }

    @Override
    public void update(UUID id, String name, String email) {
        User user = findById(id);
        if (user != null) {
            user.update(name, email);
        }
    }

    @Override
    public void delete(UUID id) {
        data.removeIf(u -> u.getId().equals(id));
    }
}