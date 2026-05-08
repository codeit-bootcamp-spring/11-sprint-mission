package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.AppConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AppConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("성공: Type이 일치하거나, ID가 IN 목록에 포함된 채널들을 정확히 조회한다")
    void findAllByTypeOrIdIn_Success_Normal() {
        // given
        Channel publicChannel = new Channel(ChannelType.PUBLIC, "공지사항", "모두가 보는 방");
        Channel privateChannel1 = new Channel(ChannelType.PRIVATE, "비밀방1", "초대된 사람만");
        Channel privateChannel2 = new Channel(ChannelType.PRIVATE, "비밀방2", "초대된 사람만");

        em.persist(publicChannel);
        em.persist(privateChannel1);
        em.persist(privateChannel2);

        em.flush();
        em.clear();

        // when
        // PUBLIC 채널 전부 OR ID가 privateChannel1인 채널
        List<UUID> searchIds = List.of(privateChannel1.getId());
        List<Channel> results = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, searchIds);

        // then
        assertThat(results).hasSize(2);

        assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder("공지사항", "비밀방1");
    }

    @Test
    @DisplayName("성공(엣지 케이스): IN 파라미터에 빈 리스트(Empty List)가 들어가면 문법 에러 없이 False로 처리된다")
    void findAllByTypeOrIdIn_EdgeCase_EmptyList() {
        // given
        Channel publicChannel = new Channel(ChannelType.PUBLIC, "공지사항", "모두가 보는 방");
        Channel privateChannel = new Channel(ChannelType.PRIVATE, "비밀방", "초대된 사람만");
        em.persist(publicChannel);
        em.persist(privateChannel);
        em.flush(); em.clear();

        // when
        List<UUID> emptyIds = Collections.emptyList();
        List<Channel> results = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, emptyIds);

        // then
        // 빈 리스트가 들어갔으므로 "OR id IN ()" 부분은 무시(또는 항상 False)되고,
        // Type이 PUBLIC인 채널(1개)만 정상적으로 반환되어야 합니다.
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("공지사항");
    }

//    @Test
//    @DisplayName("실패(엣지 케이스): IN 파라미터에 null이 들어가면 예외가 발생한다")
//    void findAllByTypeOrIdIn_EdgeCase_Null() {
//        // given
//        Channel publicChannel = new Channel(ChannelType.PUBLIC, "공지사항", "모두가 보는 방");
//        em.persist(publicChannel);
//        em.flush(); em.clear();
//
//        // when & then
//        // 컬렉션 파라미터에 null을 넘기면 Spring Data JPA가 쿼리를 생성하다가 에러를 던짐
//        assertThatThrownBy(() -> channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, null))
//                .isInstanceOf(InvalidDataAccessApiUsageException.class);
//    }

    @Test
    @DisplayName("성공(엣지 케이스): IN 파라미터에 null이 들어가도 예외 없이 안전하게 처리된다")
    void findAllByTypeOrIdIn_EdgeCase_Null() {
        // given
        Channel publicChannel = new Channel(ChannelType.PUBLIC, "공지사항", "모두가 보는 방");
        Channel privateChannel = new Channel(ChannelType.PRIVATE, "비밀방", "초대된 사람만");
        em.persist(publicChannel);
        em.persist(privateChannel);
        em.flush();
        em.clear();

        // when
        // null을 넘겨도 최신 스프링 데이터 JPA는 죽지 않고 쿼리를 안전하게 실행합니다.
        List<Channel> results = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC, null);

        // then
        // null이 들어간 IN 절은 무시(False 처리)되고, Type이 PUBLIC인 채널만 정상 조회
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("공지사항");
    }
}