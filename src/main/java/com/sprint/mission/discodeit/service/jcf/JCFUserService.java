package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import java.util.*;
import java.util.Optional;

public class JCFUserService {
    private final Map<UUID, User> data;

    public JCFUserService() {
        this.data = new HashMap<>();
    }

    public User create(String username, String email, String password) {
        User user = new User(username, email, password);
        data.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    public User update(UUID id, String username, String email, String password) {
        User user = data.get(id);

        if (user == null) {
            return null;
        }

        user.update(username, email, password);
        return user;
    }

    public void delete(UUID id) {
        data.remove(id);
    }
}
