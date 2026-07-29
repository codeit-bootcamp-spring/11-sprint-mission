package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${discodeit.admin.username}")
    private String adminUsername;

    @Value("${discodeit.admin.email}")
    private String adminEmail;

    @Value("${discodeit.admin.password}")
    private String adminPassword;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            log.info("ADMIN 계정이 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }
        User admin = new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword), null);
        admin.updateRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("ADMIN 계정 초기화 완료: username={}", adminUsername);
    }
}
