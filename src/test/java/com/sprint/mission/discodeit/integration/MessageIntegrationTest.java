package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MessageIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanDatabase() {
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
    List.of("message_attachments", "messages", "read_statuses", "user_statuses",
            "users", "channels", "binary_contents")
        .forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));
    jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
  }

  @SuppressWarnings("unchecked")
  private String createUserAndGetId(String username, String email) {
    UserCreateRequest request = new UserCreateRequest(username, email, "password123");
    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserCreateRequest> requestPart = new HttpEntity<>(request, partHeaders);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("userCreateRequest", requestPart);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    ResponseEntity<Map> response = restTemplate.postForEntity("/api/users",
        new HttpEntity<>(body, headers), Map.class);
    return (String) response.getBody().get("id");
  }

  @SuppressWarnings("unchecked")
  private String createChannelAndGetId(String name) {
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(name, null);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<Map> response = restTemplate.postForEntity("/api/channels/public",
        new HttpEntity<>(request, headers), Map.class);
    return (String) response.getBody().get("id");
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<Map> createMessage(String content, String channelId, String authorId) {
    MessageCreateRequest request = new MessageCreateRequest(
        content, UUID.fromString(channelId), UUID.fromString(authorId));
    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<MessageCreateRequest> requestPart = new HttpEntity<>(request, partHeaders);
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("messageCreateRequest", requestPart);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    return restTemplate.postForEntity("/api/messages",
        new HttpEntity<>(body, headers), Map.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  void create_succeeds() {
    String userId = createUserAndGetId("testuser", "test@example.com");
    String channelId = createChannelAndGetId("general");

    ResponseEntity<Map> response = createMessage("안녕하세요", channelId, userId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).containsEntry("content", "안녕하세요");
    assertThat(response.getBody()).containsEntry("channelId", channelId);
    assertThat(response.getBody().get("id")).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void create_withNonExistentChannel_returns404() {
    String userId = createUserAndGetId("testuser", "test@example.com");

    ResponseEntity<Map> response = createMessage("안녕하세요", UUID.randomUUID().toString(), userId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "CHANNEL_NOT_FOUND");
  }

  @Test
  @SuppressWarnings("unchecked")
  void create_withNonExistentUser_returns404() {
    String channelId = createChannelAndGetId("general");

    ResponseEntity<Map> response = createMessage("안녕하세요", channelId, UUID.randomUUID().toString());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "USER_NOT_FOUND");
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_succeeds() {
    String userId = createUserAndGetId("testuser", "test@example.com");
    String channelId = createChannelAndGetId("general");
    ResponseEntity<Map> created = createMessage("원래 내용", channelId, userId);
    String messageId = (String) created.getBody().get("id");

    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/messages/{messageId}", HttpMethod.PATCH,
        new HttpEntity<>(request, headers), Map.class, messageId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("content", "수정된 내용");
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_withNonExistentMessage_returns404() {
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/messages/{messageId}", HttpMethod.PATCH,
        new HttpEntity<>(request, headers), Map.class, UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "MESSAGE_NOT_FOUND");
  }

  @Test
  void delete_succeeds() {
    String userId = createUserAndGetId("testuser", "test@example.com");
    String channelId = createChannelAndGetId("general");
    ResponseEntity<Map> created = createMessage("삭제할 메시지", channelId, userId);
    String messageId = (String) created.getBody().get("id");

    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/messages/{messageId}", HttpMethod.DELETE, null, Void.class, messageId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @SuppressWarnings("unchecked")
  void delete_withNonExistentMessage_returns404() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/messages/{messageId}", HttpMethod.DELETE, null, Map.class, UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "MESSAGE_NOT_FOUND");
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAllByChannelId_succeeds() {
    String userId = createUserAndGetId("testuser", "test@example.com");
    String channelId = createChannelAndGetId("general");
    createMessage("첫 번째 메시지", channelId, userId);
    createMessage("두 번째 메시지", channelId, userId);

    ResponseEntity<Map> response = restTemplate.getForEntity(
        "/api/messages?channelId={channelId}", Map.class, channelId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<?> content = (List<?>) response.getBody().get("content");
    assertThat(content).hasSize(2);
    assertThat(response.getBody()).containsEntry("hasNext", false);
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAllByChannelId_whenNoMessages_returnsEmptyList() {
    String channelId = createChannelAndGetId("empty-channel");

    ResponseEntity<Map> response = restTemplate.getForEntity(
        "/api/messages?channelId={channelId}", Map.class, channelId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<?> content = (List<?>) response.getBody().get("content");
    assertThat(content).isEmpty();
    assertThat(response.getBody()).containsEntry("hasNext", false);
  }
}