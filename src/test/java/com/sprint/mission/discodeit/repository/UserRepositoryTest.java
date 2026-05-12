package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BinaryContentRepository binaryContentRepo;

    @Test
    void findByUsername_success() {
        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        );
        userRepo.save(user);

        Optional<User> result = userRepo.findByUsername("taehk23");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("taehk23");
        assertThat(result.get().getEmail()).isEqualTo("taehk23@test.com");
    }

    @Test
    void findByUsername_fail_whenUsernameNotFound() {
        Optional<User> result = userRepo.findByUsername("taehk23");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_success() {
        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        );
        userRepo.save(user);

        Optional<User> result = userRepo.findByEmail("taehk23@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("taehk23");
        assertThat(result.get().getEmail()).isEqualTo("taehk23@test.com");
    }

    @Test
    void findByEmail_fail_whenEmailNotFound() {
        Optional<User> result = userRepo.findByEmail("taehk23@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findWithStatusAndProfileById_success() {
        BinaryContent profile = new BinaryContent(
                "profile.png",
                "image/png",
                1000L
        );
        binaryContentRepo.save(profile);

        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                profile
        );
        new UserStatus(user, Instant.now());
        userRepo.save(user);

        Optional<User> result = userRepo.findWithStatusAndProfileById(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("taehk23");
        assertThat(result.get().getProfile()).isNotNull();
        assertThat(result.get().getProfile().getFileName()).isEqualTo("profile.png");
        assertThat(result.get().getStatus()).isNotNull();
    }

    @Test
    void findWithStatusAndProfileById_fail_whenUserNotFound() {
        UUID unknownUserId = UUID.randomUUID();

        Optional<User> result = userRepo.findWithStatusAndProfileById(unknownUserId);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllWithStatusAndProfile_success() {
        BinaryContent profile = new BinaryContent(
                "profile.png",
                "image/png",
                1000L
        );
        binaryContentRepo.save(profile);

        User user1 = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                profile
        );
        new UserStatus(user1, Instant.now());

        User user2 = new User(
                "user2",
                "user2@test.com",
                "password",
                null
        );
        new UserStatus(user2, Instant.now());

        userRepo.save(user1);
        userRepo.save(user2);

        List<User> result = userRepo.findAllWithStatusAndProfile();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("taehk23", "user2");
    }

    @Test
    void findAllWithStatusAndProfile_success_whenUserEmpty() {
        List<User> result = userRepo.findAllWithStatusAndProfile();

        assertThat(result).isEmpty();
    }

}
