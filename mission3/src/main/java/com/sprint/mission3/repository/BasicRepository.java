package com.sprint.mission3.repository;

import org.springframework.stereotype.Repository;

@Repository
public class BasicRepository implements RepositoryInterface {

    @Override
    public void save(String data) {
        System.out.println("기본 리포지토리: " + data + " (메모리에 저장 완료!)");
    }
}