package com.sprint.mission.discodeit.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.response.PageResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MessageIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private UserStatusRepository userStatusRepository;

  @AfterEach
  void clean_db() {
    messageRepository.deleteAllInBatch();
    userStatusRepository.deleteAllInBatch();
    channelRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  @Test
  @DisplayName("메시지 생성 통합 테스트 (멀티파트)")
  void createMessage_e2e() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    user = userRepository.save(user);
    UUID userId = user.getId();

    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channel = channelRepository.save(channel);
    UUID channelId = channel.getId();

    UserStatus userStatus = new UserStatus(user, null);
    userStatusRepository.save(userStatus); // 통합 테스트이므로 실제 데이터베이스에 저장

    MessageCreateRequest request = new MessageCreateRequest(userId, channelId, "안녕");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    HttpHeaders jsonHeader = new HttpHeaders();
    jsonHeader.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<MessageCreateRequest> jsonPart = new HttpEntity<>(request, jsonHeader);
    body.add("messageCreateRequest", jsonPart);

    HttpEntity<MultiValueMap<String, Object>> result = new HttpEntity<>(body, headers);

    //when
    ResponseEntity<MessageDto> response = restTemplate.postForEntity(
        "/api/messages", result, MessageDto.class
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().id()).isNotNull();

    MessageDto messageDto = response.getBody();
    boolean isExists = messageRepository.existsById(messageDto.id());
    assertThat(isExists).isTrue();
  }

  @Test
  @DisplayName("메시지 수정 통합 테스트")
  void updateMessage_e2e() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);

    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);

    Message message = new Message(user, channel, "과거메시지", null);
    message = messageRepository.save(message);
    UUID messageId = message.getId();

    UserStatus userStatus = new UserStatus(user, null);
    userStatusRepository.save(userStatus); // 통합 테스트이므로 실제 데이터베이스에 저장

    MessageUpdateRequest request = new MessageUpdateRequest("새메시지");

    HttpEntity<MessageUpdateRequest> result = new HttpEntity<>(request);

    //when
    ResponseEntity<MessageDto> response = restTemplate.exchange(
        "/api/messages/{messageId}",
        HttpMethod.PATCH,
        result,
        MessageDto.class,
        messageId
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response).isNotNull();
    assertThat(response.getBody().id()).isNotNull();

    Message updatedMessage = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException(messageId));
    assertThat(updatedMessage.getContent()).isEqualTo("새메시지");
  }

  @Test
  @DisplayName("채널의 메시지 목록 조회 통합 테스트 (커서x)")
  void findAllMessage_e2e() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);

    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channel = channelRepository.save(channel);
    UUID channelId = channel.getId();

    Message message1 = new Message(user, channel, "메시지1", null);
    Message message2 = new Message(user, channel, "메시지2", null);
    messageRepository.saveAll(List.of(message1, message2));

    //when
    ResponseEntity<PageResponse<MessageDto>> response = restTemplate.exchange(
        "/api/messages?channelId={channelId}", // @RequestParam(쿼리 파라미터) 사용
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<PageResponse<MessageDto>>() { // 컨트롤러의 반환 타입이 PageResponse 안에 MessageDto가 들어있는 '제네릭 타입'이므로 ParameterizedTypeReference를 사용해 정확히 명시해줌
        },
        channelId
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().content()).hasSize(2);
  }

  @Test
  @DisplayName("메시지 삭제 통합 테스트")
  void deleteMessage_e2e() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    userRepository.save(user);

    Channel channel = new Channel("채팅방", "채팅방", ChannelType.PUBLIC);
    channelRepository.save(channel);

    Message message = new Message(user, channel, "안녕", null);
    message = messageRepository.save(message);
    UUID messageId = message.getId();

    //when
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/messages/{messageId}",
        HttpMethod.DELETE,
        null,
        Void.class,
        messageId
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    boolean isExists = messageRepository.existsById(messageId);
    assertThat(isExists).isFalse();
  }
}
