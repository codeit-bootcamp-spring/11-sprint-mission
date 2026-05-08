package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.AppConfig;
import com.sprint.mission.discodeit.entity.*;
import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("test")
@Import(AppConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReadStatusRepositoryTest {

    @Autowired
    ReadStatusRepository readStatusRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("성공: 특정 채널의 ReadStatus와 연관된 User 전체 정보 Fetch Join 조회")
    void findAllByChannelIdWithUser_Success() {
        Channel channel = new Channel(ChannelType.PUBLIC, "공지방", "공지방");
        em.persist(channel);

        BinaryContent profile = new BinaryContent("profile.png", 1024L, "image/png");
        User user = new User("tester", "test@email.com", "password123", profile);
        UserStatus userStatus = new UserStatus(user, Instant.now());

        em.persist(user);

        ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
        em.persist(readStatus);

        em.flush();
        em.clear();

        // when
        List<ReadStatus> results = readStatusRepository.findAllByChannelIdWithUser(channel.getId());

        // then
        assertThat(results).hasSize(1);

        ReadStatus foundReadStatus = results.get(0);
        User foundUser = foundReadStatus.getUser();
        assertThat(foundUser.getUsername()).isEqualTo("tester");
        assertThat(foundUser.getProfile().getContentType()).isEqualTo("image/png");
        assertThat(foundUser.getStatus().getLastActiveAt()).isNotNull();
    }
}

// TestEntityManager vs Repository
// - repository.save()는 이거 자체적으로 문제가 발생할 수 있음
// - TestEntityManager(EntityManager) 데이터베이스와 직접적으로 연결된 진짜
// - repository.saveAndFlush()가 잇음 그리고 이게 em.persist()랑 사실상 동일함
// - 근데 사용하지 않는 이유는 분리 => 테스트 대상: repository이므로 영속화를 하는건 em을 따로 둬서 직접 영속화를 시킴
// - 그리고 추가적으로 repository에는 clear()가 없음

// em.flush() / em.clear()
//      - 1차 캐시: 데이터베이스에 가기 전에 1차 캐시에 잠깐 머무름
//      - 거짓 성공의 함정: 조회를 하게 될 경우 1차 캐시에 있으면 DB로 select 쿼리를 날리지 않고 해당 객체를 그냥 반환해버림
// - em.flush(): DB로 쿼리 즉시 쏘기
// - em.clear(): 1차 캐시를 백지화