package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FileUserRepository extends CommonFileRepository<User> implements UserRepository {
    public FileUserRepository() {
        super("users", User.class);
    }

    @Override
    public boolean existsByName(String name) {
        List<User> userList = findAll();
        for(User user : userList) {
            if(user.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
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
