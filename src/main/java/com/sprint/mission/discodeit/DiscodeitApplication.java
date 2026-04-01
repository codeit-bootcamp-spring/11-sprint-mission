package com.sprint.mission.discodeit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 예전 main에서는 객체생성, 의존성연결, 테스트실행 모두 담당
// AppConfig - 객체생성 및 연결
// TestDataRunner - 테스트
// DiscodeitApplication - 앱 시작
@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
                SpringApplication.run(DiscodeitApplication.class, args);

    }
}