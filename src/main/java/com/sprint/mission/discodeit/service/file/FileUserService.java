package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class FileUserService extends FileUtil implements UserService {
    public FileUserService() {
        super("users");
    }

    @Override
    public User create(String name, String email, String password) {
        User user = new User(name, email, password);
        save(filePath(user.getId()), user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        Path path = filePath(id);
        if(!Files.exists(path)) {
            throw new IllegalArgumentException("User Not Found");
        }
        return load(path, User.class);
    }

    @Override
    public List<User> findAll() {
        return loadAll(directory, User.class);
    }

    @Override
    public void update(User oldUser, User newUser) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        oldUser.setName(newUser.getName());
        oldUser.setPassword(newUser.getPassword());
        oldUser.setEmail(newUser.getEmail());
        oldUser.update();
        save(filePath(oldUser.getId()), oldUser);
    }

    @Override
    public void delete(User user) {
        delete(filePath(user.getId()));
    }
}
