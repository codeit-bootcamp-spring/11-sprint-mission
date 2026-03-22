package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileUserRepository implements UserRepository {
    private final FileIOUtil<User> fileIOUtil;

    public FileUserRepository() {
        this.fileIOUtil = new FileIOUtil<>(User.class);
    }

    @Override
    public void save(User user) {
        this.fileIOUtil.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(this.fileIOUtil.findById(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return this.fileIOUtil.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return this.fileIOUtil.findAll();
    }

    @Override
    public boolean existById(UUID id) {
        return this.fileIOUtil.findById(id) != null;
    }

    @Override
    public boolean existByUsername(String username) {
        return this.fileIOUtil.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    @Override
    public boolean existByEmail(String email) {
        return this.fileIOUtil.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public void delete(User user) {
        this.fileIOUtil.delete(user);
    }
}
