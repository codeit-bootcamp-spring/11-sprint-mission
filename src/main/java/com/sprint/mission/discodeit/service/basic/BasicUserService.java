package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(String name, String email, String password) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다. 다른 이메일을 사용해주세요.");
        }
        User user = new User(name, email, password);
        User savedUser = userRepository.save(user);
        System.out.println("유저 생성완료! (이름 : " + savedUser.getUsername() + ")");
        return savedUser;
    }

    @Override
    public User readUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("입력한 이메일이 존재하지 않습니다."));
    }

    @Override
    public List<User> allReadUser() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(UUID id, String password) {
        User user = readUser(id);
        if(!user.getPassword().equals(password)) {
            throw new InputMismatchException("패스워드가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }

    @Override
    public void updateUser(UUID id, String name, String email, String password) {
        User user = readUser(id);
        userRepository.findByEmail(email)
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {throw new IllegalArgumentException("이미 사용중인 이메일 입니다.");});

        user.update(name, email, password);
        userRepository.save(user);
    }
}
