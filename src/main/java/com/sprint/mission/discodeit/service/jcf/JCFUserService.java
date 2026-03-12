package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFUserService implements UserService {
    final List<User> userList;

    public JCFUserService() {
        userList = new ArrayList<>();
    }

    @Override
    public User create(String name, String email, String password) {
        User user = new User(name, email, password);
        userList.add(user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        for(User user: userList) {
            if(user.getId().equals(id)) return user;
        }
        throw new IllegalArgumentException("User Not Found");
    }

    @Override
    public List<User> findAll() {
        return userList;
    }

    @Override
    public void update(User oldUser, User newUser) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        oldUser.setName(newUser.getName());
        oldUser.setPassword(newUser.getPassword());
        oldUser.setEmail(newUser.getEmail());
        oldUser.update();
    }

    @Override
    public void delete(User user) {
        userList.remove(user);
    }
}






