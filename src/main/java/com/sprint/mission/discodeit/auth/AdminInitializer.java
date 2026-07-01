package com.sprint.mission.discodeit.auth;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean hasAdmin = userRepository.existsByRole(Role.ADMIN);

        if (!hasAdmin) {
            log.info("어드민 계정 초기화");

            User admin = User.createAdmin(
                    "admin",
                    "admin@email.com",
                    passwordEncoder.encode("admin1234!")
            );

            userRepository.save(admin);
        }
    }
}
