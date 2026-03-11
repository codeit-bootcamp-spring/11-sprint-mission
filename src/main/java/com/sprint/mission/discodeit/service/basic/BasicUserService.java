package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Domain.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class BasicUserService implements UserService {
    private final UserRepository userRepository;

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UUID create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("유저가 null입니다.");
        }
        if (user.getUserName() == null) {
            throw new IllegalArgumentException("유저명이 null입니다.");
        }
        if (user.getUserName().isBlank()) {
            throw new IllegalArgumentException("유저명이 blank입니다.");
        }
        boolean isDuplicate = userRepository.readAll().stream()
                .anyMatch(u -> u.getUserName().equals(user.getUserName()));
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }
        return userRepository.create(user);
    }

    @Override
    public User read(UUID id) {
        return userRepository.read(id);
    }

    @Override
    public List<User> readAll() {
        return userRepository.readAll();
    }

    @Override
    public void update(UUID id, String userName, String userNickname, String userStatus) {
        User user = userRepository.read(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if (userName == null) {
            throw new IllegalArgumentException("유저명이 null입니다.");
        }
        if (userName.isBlank()) {
            throw new IllegalArgumentException("유저명이 blank입니다.");
        }
        boolean isDuplicate = userRepository.readAll().stream()
                .filter(u -> !u.getId().equals(id))
                .anyMatch(u -> u.getUserName().equals(userName));
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }
        user.updateUserName(userName, userNickname, userStatus);
        userRepository.create(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.read(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        userRepository.delete(id);
    }
}