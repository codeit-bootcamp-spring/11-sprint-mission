package com.sprint.mission3.service;

import com.sprint.mission3.repository.RepositoryInterface;
import org.springframework.stereotype.Service;

@Service
public class MissionService {

    private final RepositoryInterface repository;

    public MissionService(RepositoryInterface repository) {
        this.repository = repository;
    }

    public void run() {
        System.out.println("서비스가 실행됩니다!");
        repository.save("미션 데이터 저장 완료");
    }
}