package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.userdto.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
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
class UserIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  @DisplayName("사용자 생성 후 조회")
  void createUser() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    UserCreateRequest requestDto = new UserCreateRequest("e2e", "e2e@test.com", "pass1234");

    HttpHeaders jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserCreateRequest> requestPart = new HttpEntity<>(requestDto, jsonHeaders);
    body.add("userCreateRequest", requestPart);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<UserDto> response = restTemplate.postForEntity(
        "/api/users", requestEntity, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().username()).isEqualTo("e2e");

    UUID userId = response.getBody().id();

    ResponseEntity<UserDto> response2 = restTemplate.getForEntity(
        "/api/users/" + userId, UserDto.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response2.getBody().username()).isEqualTo("e2e");

  }


  @Test
  @DisplayName("사용자 생성 후 업데이트")
  void updateUser() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    UserCreateRequest requestDto = new UserCreateRequest("e2e", "e2e@test.com", "pass1234");

    HttpHeaders jsonHeaders = new HttpHeaders();
    jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserCreateRequest> requestPart = new HttpEntity<>(requestDto, jsonHeaders);
    body.add("userCreateRequest", requestPart);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<UserDto> response = restTemplate.postForEntity(
        "/api/users", requestEntity, UserDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().username()).isEqualTo("e2e");

    UUID userId = response.getBody().id();

    //업데이트
    MultiValueMap<String, Object> updateBody = new LinkedMultiValueMap<>();
    UserUpdateRequest requestDto2 = new UserUpdateRequest("updateUser", "newPassword",
        "update@test.com");
    HttpEntity<UserUpdateRequest> requestPart2 = new HttpEntity<>(requestDto2, jsonHeaders);
    updateBody.add("userUpdateRequest", requestPart2);

    HttpEntity<MultiValueMap<String, Object>> requestEntity2 = new HttpEntity<>(updateBody,
        headers);

    restTemplate.patchForObject(
        "/api/users/" + userId, requestEntity2, UserDto.class);

    //조회

    ResponseEntity<UserDto> response2 = restTemplate.getForEntity(
        "/api/users/" + userId, UserDto.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response2.getBody().username()).isEqualTo("updateUser");
    assertThat(response2.getBody().email()).isEqualTo("update@test.com");


  }


}
