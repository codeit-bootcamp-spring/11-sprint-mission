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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@Import(AppConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MessageRepositoryTest {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("성공: Slice를 활용한 커서 기반 메시지 페이징 조회 (No-Offset)")
    void findAllByChannelIdWithAuthor_Slice_Success() throws InterruptedException {
        // given
        Channel channel = new Channel(ChannelType.PUBLIC, "자유-채널", "자유롭게 대화하세요");
        em.persist(channel);

        BinaryContent profile = new BinaryContent("profile.png", 1024L, "image/png");
        User user = new User("chatter", "chat@email.com", "password", profile);
        new UserStatus(user, Instant.now());
        em.persist(user);

        // - 시간차로 생성 시간 벌리기
        Message msg1 = new Message("첫 번째 메시지", channel, user, Collections.emptyList());
        em.persist(msg1); Thread.sleep(10);
        Message msg2 = new Message("두 번째 메시지", channel, user, Collections.emptyList());
        em.persist(msg2); Thread.sleep(10);
        Message msg3 = new Message("세 번째 메시지", channel, user, Collections.emptyList());
        em.persist(msg3); Thread.sleep(10);
        Message msg4 = new Message("네 번째 메시지", channel, user, Collections.emptyList());
        em.persist(msg4); Thread.sleep(10);
        Message msg5 = new Message("다섯 번째 메시지", channel, user, Collections.emptyList());
        em.persist(msg5);

        em.flush();
        em.clear();

        // - DB와의 시간 정밀도 차이가 있기 때문에 가져와서 하는게 좋음
        Message savedMsg5 = messageRepository.findById(msg5.getId()).orElseThrow();
        Instant cursorTime = savedMsg5.getCreatedAt();
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Slice<Message> messageSlice = messageRepository.findAllByChannelIdWithAuthor(
                channel.getId(),
                cursorTime,
                pageRequest
        );

        // then
        List<Message> content = messageSlice.getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).getContent()).isEqualTo("네 번째 메시지");
        assertThat(content.get(1).getContent()).isEqualTo("세 번째 메시지");

        assertThat(messageSlice.hasNext()).isTrue();

        assertThat(content.get(0).getAuthor().getUsername()).isEqualTo("chatter");
    }
}
