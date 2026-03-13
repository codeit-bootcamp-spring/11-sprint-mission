package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.dto.UserCreateRequestDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.DuplicateNameException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
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
    public User create(UserCreateRequestDto dto) {
        if(existsByName(dto.name())) throw new DuplicateNameException(dto.name());
        if(existsByEmail(dto.email())) throw new DuplicateEmailException(dto.email());

        User user = new User(dto.name(), dto.email(), dto.password(), dto.profileId());
        save(filePath(user.getId()), user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        Path path = filePath(id);
        if(!Files.exists(path)) {
            throw new UserNotFoundException(id);
        }
        return load(path, User.class);
    }

    @Override
    public List<User> findAll() {
        return loadAll(directory, User.class);
    }

    @Override
    public void update(UUID id, User newUser) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        User oldUser = findById(id);

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

    public boolean existsByName(String name) {
        List<User> userList = findAll();
        for(User user : userList) {
            if(user.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsByEmail(String email) {
        List<User> userList = findAll();
        for(User user : userList) {
            if(user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
}
