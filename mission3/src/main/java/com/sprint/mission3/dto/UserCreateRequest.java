package com.sprint.mission3.service;

import com.sprint.mission3.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void registerUser(String name) {
        System.out.println("UserService: 유저 등록 로직을 실행합니다.");

    }
}