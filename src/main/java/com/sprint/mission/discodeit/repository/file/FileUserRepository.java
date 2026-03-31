package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "file"
)
public class FileUserRepository extends CommonFileRepository<User> implements UserRepository {
    public FileUserRepository(
            @Value("${discodeit.repository.file.base-dir}") String basedir
    ) {
        super("users", User.class, basedir);
    }

    @Override
    public Optional<User> findByName(String name) {
        List<User> userList = findAll();
        for(User user : userList) {
            if(user.getUsername().equals(name)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> userList = findAll();
        for(User user : userList) {
            if(user.getEmail().equals(email)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
