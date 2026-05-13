package com.sprint.mission.discodeit.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class UserIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("사용자 생성 API E2E 테스트")
  void createUser_e2e_success() {
    UserCreateRequest request = new UserCreateRequest("e2eUser",
        "e2e@test.com", "pass1234", null);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("userCreateRequest", request);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<UserDto> response = restTemplate.postForEntity(
        "/api/users", requestEntity, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().username()).isEqualTo("e2eUser");
  }

  @Test
  @DisplayName("사용자 목록 조회 API E2E 테스트")
  void getAllUsers_e2e_success() {
    registerUserViaApi("user1", "user1@test.com");
    registerUserViaApi("user2", "user2@test.com");

    ResponseEntity<UserDto[]> response = restTemplate.getForEntity(
        "/api/users", UserDto[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
  }

  private void registerUserViaApi(String username, String email) {
    UserCreateRequest request = new UserCreateRequest(username, email, "pass1234", null);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("userCreateRequest", request);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    restTemplate.postForEntity("/api/users", new HttpEntity<>(body, headers), UserDto.class);
  }
}
