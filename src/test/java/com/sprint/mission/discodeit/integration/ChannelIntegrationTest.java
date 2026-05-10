package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
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
class ChannelIntegrationTest {

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
  private ResponseEntity<Map> createPublicChannel(String name, String description) {
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(name, description);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.postForEntity("/api/channels/public",
        new HttpEntity<>(request, headers), Map.class);
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<Map> createPrivateChannel(List<UUID> participantIds) {
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.postForEntity("/api/channels/private",
        new HttpEntity<>(request, headers), Map.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  void createPublic_succeeds() {
    ResponseEntity<Map> response = createPublicChannel("general", "공개 채널");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).containsEntry("type", "PUBLIC");
    assertThat(response.getBody()).containsEntry("name", "general");
    assertThat(response.getBody().get("id")).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void createPublic_withEmptyName_returns400() {
    ResponseEntity<Map> response = createPublicChannel("", null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("code", "VALIDATION_FAILED");
  }

  @Test
  @SuppressWarnings("unchecked")
  void createPrivate_succeeds() {
    String userId = createUserAndGetId("user1", "user1@example.com");

    ResponseEntity<Map> response = createPrivateChannel(List.of(UUID.fromString(userId)));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).containsEntry("type", "PRIVATE");
  }

  @Test
  @SuppressWarnings("unchecked")
  void createPrivate_withEmptyParticipantIds_returns400() {
    ResponseEntity<Map> response = createPrivateChannel(List.of());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("code", "VALIDATION_FAILED");
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_succeeds() {
    ResponseEntity<Map> created = createPublicChannel("old-name", "이전 설명");
    String channelId = (String) created.getBody().get("id");

    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("new-name", "새 설명");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/channels/{channelId}", HttpMethod.PATCH,
        new HttpEntity<>(request, headers), Map.class, channelId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("name", "new-name");
    assertThat(response.getBody()).containsEntry("description", "새 설명");
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_onPrivateChannel_returns403() {
    String userId = createUserAndGetId("user1", "user1@example.com");
    ResponseEntity<Map> created = createPrivateChannel(List.of(UUID.fromString(userId)));
    String channelId = (String) created.getBody().get("id");

    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("new-name", "설명");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/channels/{channelId}", HttpMethod.PATCH,
        new HttpEntity<>(request, headers), Map.class, channelId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).containsEntry("code", "PRIVATE_CHANNEL_UPDATE");
    assertThat(response.getBody().get("status")).isEqualTo(403);
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_withNonExistentChannel_returns404() {
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("new-name", "설명");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/channels/{channelId}", HttpMethod.PATCH,
        new HttpEntity<>(request, headers), Map.class, UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "CHANNEL_NOT_FOUND");
  }

  @Test
  void delete_succeeds() {
    ResponseEntity<Map> created = createPublicChannel("general", null);
    String channelId = (String) created.getBody().get("id");

    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/channels/{channelId}", HttpMethod.DELETE, null, Void.class, channelId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @SuppressWarnings("unchecked")
  void delete_withNonExistentChannel_returns404() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/channels/{channelId}", HttpMethod.DELETE, null, Map.class, UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "CHANNEL_NOT_FOUND");
  }
}