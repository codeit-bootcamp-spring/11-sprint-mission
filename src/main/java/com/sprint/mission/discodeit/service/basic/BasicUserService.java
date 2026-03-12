package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepo;

    @Override
    public User create(String name, String email, String password) {
        User user = new User(name, email, password);
        userRepo.save(user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public List<User> findAll() {
        return userRepo.findAll();
    }

    @Override
    public void update(User oldUser, User newUser) {
        oldUser.setName(newUser.getName());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setPassword(newUser.getPassword());
        oldUser.update();
        userRepo.save(oldUser);
    }

    @Override
    public void delete(User user) {
        userRepo.delete(user);
    }
}
