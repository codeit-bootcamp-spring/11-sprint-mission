package com.sprint.mission3.repository;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Primary;

@Primary
@Repository
public class FileRepository implements RepositoryInterface {

    @Override
    public void save(String data) {
        System.out.println("파일 리포지토리: " + data + " (파일에 저장 완료!)");
    }
}