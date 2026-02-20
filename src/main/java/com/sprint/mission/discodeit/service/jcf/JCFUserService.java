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
    public void createUser(User user) {
        userList.add(user);
    }

    @Override
    public User findUser(UUID id) {
        for(User user: userList) {
            if(user.getId().equals(id)) return user;
        }
        throw new IllegalArgumentException("User Not Found");
    }

    @Override
    public List<User> findAllUser() {
        return userList;
    }

    @Override
    public void updateUser(User oldUser, User newUser) {
        // UUID를 유지하기 위해 remove -> add 하지 않음
        oldUser.setUserId(newUser.getUserId());
        oldUser.setName(newUser.getName());
        oldUser.setPassword(newUser.getPassword());
        oldUser.setEmail(newUser.getEmail());
        oldUser.update();
    }

    @Override
    public void deleteUser(User user) {
        userList.remove(user);
    }
}






