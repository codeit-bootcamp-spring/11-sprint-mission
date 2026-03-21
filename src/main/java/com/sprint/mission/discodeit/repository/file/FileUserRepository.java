package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class FileUserRepository extends FileRepository<User> implements UserRepository {
    public FileUserRepository() {
        super(User.class);
    }

    @Override
    public void save(User user) {
        super.save(user);
    }

    @Override
    public User findById(UUID id) {
        User user = super.findById(id);
        if (user == null) throw new IllegalArgumentException("requested user not found. ❌");

        return user;
    }

    @Override
    public boolean existByUsername(String username) {
        return super.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    @Override
    public boolean existByEmail(String email) {
        return super.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public List<User> findAll() {
        return super.findAll();
    }

    @Override
    public void delete(User user) {
        super.delete(user);
    }
}
