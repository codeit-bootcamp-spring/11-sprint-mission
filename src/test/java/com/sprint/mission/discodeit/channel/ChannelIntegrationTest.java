package com.sprint.mission.discodeit.channel;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChannelIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private UserRepository userRepository;

  @AfterEach
  void clean_db() {
    channelRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("퍼블릭 채널 생성 통합 테스트")
  void createPublic_e2e() {
    //given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("채팅방", "채팅방");

    //when
    ResponseEntity<ChannelDto> response = restTemplate.postForEntity(
        "/api/channels/public", request, ChannelDto.class
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().name()).isEqualTo("채팅방");

    boolean isExistInDb = channelRepository.existsById(response.getBody().id());
    assertThat(isExistInDb).isTrue();
  }

  @Test
  @DisplayName("프라이빗 채널 생성 통합 테스트")
  void createPrivate_e2e() {
    //given
    User user1 = new User("마크", "mark@test.com", "pass1234", null);
    User user2 = new User("민형", "mh@test.com", "pass1234", null);
    userRepository.saveAll(List.of(user1, user2));

    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        List.of(user1.getId(), user2.getId()));

    //when
    ResponseEntity<ChannelDto> response = restTemplate.postForEntity(
        "/api/channels/private", request, ChannelDto.class
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response).isNotNull();

    ChannelDto resultDto = response.getBody();
    assertThat(resultDto.type()).isEqualTo(ChannelType.PRIVATE);
    assertThat(resultDto.id()).isNotNull();

    boolean isExistInDb = channelRepository.existsById(resultDto.id());
    assertThat(isExistInDb).isTrue();
  }

  @Test
  @DisplayName("채널 수정 통합 테스트")
  void updateChannel_e2e() {
    Channel oldChannel = new Channel("기존채팅방", "기존채팅방", ChannelType.PUBLIC);
    channelRepository.save(oldChannel);
    UUID channelId = oldChannel.getId();

    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새채팅방", "새채팅방");

    HttpEntity<PublicChannelUpdateRequest> result = new HttpEntity<>(
        request); // HttpEntity로 포장. @RequestBody를 쓰기 때문에 헤더 설정이 필요 없음 (스프링이 알아서 JSON으로 보냄)

    ResponseEntity<ChannelDto> response = restTemplate.exchange(
        "/api/channels/{channelId}",
        HttpMethod.PATCH,
        result, // exchange() 메서드를 쓸 땐 무조건 HttpEntity 객체를 보내줘야 함
        ChannelDto.class,
        channelId
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id()).isNotNull();

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(channelId));
    assertThat(channel.getName()).isEqualTo("새채팅방");
  }

  @Test
  @DisplayName("채널 삭제 통합 테스트")
  void deleteChannel_e2e() {
    //given
    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);
    UUID channelId = channel.getId();

    //when
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/channels/{channelId}",
        HttpMethod.DELETE,
        null,
        Void.class,
        channelId
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    boolean isExist = channelRepository.existsById(channelId);
    assertThat(isExist).isFalse();
  }

}
