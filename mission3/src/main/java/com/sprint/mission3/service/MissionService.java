package com.sprint.mission3.service;

import com.sprint.mission3.repository.RepositoryInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionService {

    // 이제 리포지토리를 직접 부르지 않고, UserService(전문가)를 부릅니다.
    private final UserService userService;

    public void run() {
        System.out.println("MissionService: 매니저가 업무를 배분합니다.");

        // 유저 서비스에게 일을 시킵니다.
        userService.registerUser("최우준");

        System.out.println("MissionService: 모든 업무 지시 완료!");
    }
}