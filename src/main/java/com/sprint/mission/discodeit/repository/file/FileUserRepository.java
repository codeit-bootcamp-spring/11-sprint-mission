package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FileUserRepository extends CommonFileRepository<User> implements UserRepository {
    public FileUserRepository() {
        super("users", User.class);
    }
}
