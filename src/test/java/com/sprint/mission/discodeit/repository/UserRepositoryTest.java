package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.AppConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
// - 기본 동작(임베디드 DB 대체)
// - JPA 컴포넌트들만 컨텍스트에 로드함
// - @Transactional이 포함되어 있어 자동으로 롤백됨
@ActiveProfiles("test")
// - application-test.yaml 실행
@Import(AppConfig.class)
// - main에 @EnableJpaAuditing이 있으면 이건 로드하지 않아 제대로 동작이 안됨
// - 그래서 직접 가져와서 설정 구성
@AutoConfigureTestDatabase(replace = Replace.NONE)
// - @DataJpaTest가 application-test.yaml의
// - datasource.url 속성을 덮어버리기 때문에 내가 직접 설정한 환경으로 동작하기 위해서 사용
class UserRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired TestEntityManager em; // 영속성 컨텍스트를 직접 제어하기 위한 빈

    @Test
    @DisplayName("성공: username으로 사용자 조회")
    void findByUsername_Success() {
        // given
        User user = new User("testUser", "test@email.com", "password123", null);
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByUsername(user.getUsername());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("실패: username으로 사용자 조회")
    void findByUsername_Fail() {
        // given
        User user = new User("testUser", "test@email.com", "password123", null);
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByUsername("mockUser");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("성공: Profile(BinaryContent)과 Status를 포함한 User 다중 Fetch Join 조회")
    void findAllWithProfileAndStatus_Success() {
        // given
        BinaryContent profile = new BinaryContent("profile.png", 1024L, "image/png");
        User user = new User("tester", "test@email.com", "password123", profile);

        UserStatus userStatus = new UserStatus(user, Instant.now());

        em.persist(user);

        em.flush();
        em.clear();

        // when
        List<User> users = userRepository.findAllWithProfileAndStatus();

        // then
        assertThat(users).hasSize(1);

        User foundUser = users.get(0);
        assertThat(foundUser.getUsername()).isEqualTo("tester");

        assertThat(foundUser.getProfile().getFileName()).isEqualTo("profile.png");
        assertThat(foundUser.getStatus().isOnline()).isTrue();
    }
}