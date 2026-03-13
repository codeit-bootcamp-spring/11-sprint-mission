package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.List;

public class JCFUserRepository extends CommonJCFRepository<User> implements UserRepository {
    public JCFUserRepository() {
        super();
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
