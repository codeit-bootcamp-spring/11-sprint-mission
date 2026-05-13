package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
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
class UserIntegrationTest {

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
  private ResponseEntity<Map> createUser(String username, String email, String password) {
    UserCreateRequest request = new UserCreateRequest(username, email, password);

    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserCreateRequest> requestPart = new HttpEntity<>(request, partHeaders);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("userCreateRequest", requestPart);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return restTemplate.postForEntity("/api/users",
        new HttpEntity<>(body, headers), Map.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  void create_succeeds() {
    ResponseEntity<Map> response = createUser("testuser", "test@example.com", "password123");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).containsEntry("username", "testuser");
    assertThat(response.getBody()).containsEntry("email", "test@example.com");
    assertThat(response.getBody().get("id")).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void create_withDuplicateUsername_returns409() {
    createUser("dupuser", "first@example.com", "password123");
    ResponseEntity<Map> response = createUser("dupuser", "second@example.com", "password123");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).containsEntry("code", "DUPLICATE_USER");
    assertThat(response.getBody().get("status")).isEqualTo(409);
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_succeeds() {
    ResponseEntity<Map> created = createUser("testuser", "test@example.com", "password123");
    String userId = (String) created.getBody().get("id");

    UserUpdateRequest request = new UserUpdateRequest("newname", "new@example.com", "newpassword1");
    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserUpdateRequest> requestPart = new HttpEntity<>(request, partHeaders);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("userUpdateRequest", requestPart);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/users/{userId}", HttpMethod.PATCH,
        new HttpEntity<>(body, headers), Map.class, userId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("username", "newname");
    assertThat(response.getBody()).containsEntry("email", "new@example.com");
  }

  @Test
  @SuppressWarnings("unchecked")
  void update_withNonExistentUser_returns404() {
    UserUpdateRequest request = new UserUpdateRequest("newname", "new@example.com", "newpassword1");
    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserUpdateRequest> requestPart = new HttpEntity<>(request, partHeaders);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("userUpdateRequest", requestPart);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/users/{userId}", HttpMethod.PATCH,
        new HttpEntity<>(body, headers), Map.class, UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "USER_NOT_FOUND");
  }

  @Test
  void delete_succeeds() {
    ResponseEntity<Map> created = createUser("testuser", "test@example.com", "password123");
    String userId = (String) created.getBody().get("id");

    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/users/{userId}", HttpMethod.DELETE, null, Void.class, userId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @SuppressWarnings("unchecked")
  void delete_withNonExistentUser_returns404() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "/api/users/{userId}", HttpMethod.DELETE, null, Map.class, UUID.randomUUID());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("code", "USER_NOT_FOUND");
  }

  @Test
  @SuppressWarnings("unchecked")
  void findAll_succeeds() {
    createUser("user1", "user1@example.com", "password123");
    createUser("user2", "user2@example.com", "password123");

    ResponseEntity<List> response = restTemplate.getForEntity("/api/users", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(2);
  }

  @Test
  void findAll_whenNoUsers_returnsEmptyList() {
    ResponseEntity<List> response = restTemplate.getForEntity("/api/users", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }
}