package com.sprint.mission.discodeit.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class MessageIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  @DisplayName("메시지 생성 API E2E 테스트")
  void createMessage_e2e_success() {
    UserDto user = registerUserViaApi("msgUser", "msg@test.com");
    ChannelDto channel = createChannelViaApi("msgChannel");

    MessageCreateRequest request = new MessageCreateRequest(
        "통합 테스트 메시지", user.id(), channel.id());

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("messageCreateRequest", request);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    ResponseEntity<MessageDto> response = restTemplate.postForEntity(
        "/api/messages", new HttpEntity<>(body, headers), MessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().content()).isEqualTo("통합 테스트 메시지");
  }

  @Test
  @DisplayName("채널별 메시지 목록 조회 API E2E 테스트")
  void getMessagesByChannel_e2e_success() {
    UserDto user = registerUserViaApi("msgListUser", "msglist@test.com");
    ChannelDto channel = createChannelViaApi("msgListChannel");

    createMessageViaApi("첫 번째 메시지", user.id(), channel.id());
    createMessageViaApi("두 번째 메시지", user.id(), channel.id());

    ResponseEntity<String> response = restTemplate.getForEntity(
        "/api/messages?channelId=" + channel.id(), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).contains("첫 번째 메시지", "두 번째 메시지");
  }

  private UserDto registerUserViaApi(String username, String email) {
    UserCreateRequest request = new UserCreateRequest(username, email,
        "pass1234", null);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("userCreateRequest", request);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    return restTemplate.postForEntity("/api/users",
        new HttpEntity<>(body, headers), UserDto.class).getBody();
  }

  private ChannelDto createChannelViaApi(String name) {
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(
        name, "설명");
    return restTemplate.postForEntity("/api/channels/public",
        request, ChannelDto.class).getBody();
  }

  private void createMessageViaApi(String content, UUID authorId, UUID channelId) {
    MessageCreateRequest request = new MessageCreateRequest(content, authorId, channelId);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("messageCreateRequest", request);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    restTemplate.postForEntity("/api/messages", new HttpEntity<>(
        body, headers), MessageDto.class);
  }
}