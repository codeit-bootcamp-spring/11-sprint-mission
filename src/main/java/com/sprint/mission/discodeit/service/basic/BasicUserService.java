package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BasicUserService implements UserService {

    UserRepository userRepo;

    public BasicUserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User createUser(String name, String email, String password) {
        User user = new User(name, email, password);
        userRepo.save(user);
        return user;
    }

    @Override
    public User findUser(UUID id) {
        return userRepo.load(id);
    }

    @Override
    public List<User> findAllUser() {
        return userRepo.loadAll();
    }

    @Override
    public void updateUser(User oldUser, User newUser) {
        oldUser.setName(newUser.getName());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setPassword(newUser.getPassword());
        oldUser.update();
        userRepo.save(oldUser);
    }

    @Override
    public void deleteUser(User user) {
        userRepo.delete(user);
    }
}
