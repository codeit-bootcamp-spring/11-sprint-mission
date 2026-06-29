package com.sprint.mission.discodeit.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
public class UserIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate; // 통합 테스트에서는 MockMvc가 아닌 restTemplate을 사용하는게 나은지?

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @AfterEach
  void clean_db() {
    userRepository.deleteAllInBatch(); // 매 테스트 후 모든 유저 데이터를 깔끔하게 삭제
  }

  @Test
  @DisplayName("사용자 생성 통합 테스트 (멀티파트)")
  void createUser_e2e() throws Exception {
    //given
    // 요청 DTO
    UserCreateRequest request = new UserCreateRequest("마크", "mark@test.com", "pass1234");

    // 헤더에 멀티파트 폼 데이터임을 명시
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // 멀티파트 조각 - JSON(DTO), MultipartFile(profile)을 담을 바디(Map) 준비
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    // JSON DTO를 바이트 배열이 아닌 JSON 타입으로 지정하여 컨트롤러가 읽기 쉬운 헤더가 달린 조각으로 묶어서 바디에 넣음
    HttpHeaders jsonHeader = new HttpHeaders();
    jsonHeader.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> jsonPart = new HttpEntity<>(objectMapper.writeValueAsString(request),
        jsonHeader); // HttpEntity<>(body, headers)
    body.add("userCreateRequest", jsonPart); // .add(Key, Value)

    // 최종적으로 완성된 헤더와 바디를 포장
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    //when: 실제 POST 요청을 날림
    ResponseEntity<UserDto> response = restTemplate.postForEntity("/api/users", requestEntity,
        UserDto.class); // 주소, 보낼 데이터, 받을 타입

    //then: DB까지 다녀와서 검증
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().username()).isEqualTo("마크");
  }

  @Test
  @DisplayName("사용자 수정 통합 테스트 (멀티파트)")
  void updateUser_e2e() throws Exception {
    //given
    // 실제로 수정할 기존 유저를 DB에 먼저 생성
    User oldUser = new User("마크", "mark@test.com", "pass1234", null);
    oldUser = userRepository.save(oldUser);
    UUID oldUserId = oldUser.getId();

    UserUpdateRequest request = new UserUpdateRequest("민형", "mh@test.com", "pass1234");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

    HttpHeaders jsonHeader = new HttpHeaders();
    jsonHeader.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> jsonPart = new HttpEntity<>(objectMapper.writeValueAsString(request),
        jsonHeader);
    body.add("userUpdateRequest", jsonPart);

    HttpEntity<MultiValueMap<String, Object>> result = new HttpEntity<>(body, headers);

    //when
    // PATCH 요청은 exchange()를 사용해야 함
    ResponseEntity<UserDto> response = restTemplate.exchange(
        "/api/users/{userId}",
        HttpMethod.PATCH, // HTTP 메서드
        result,
        UserDto.class,
        oldUserId // URL의 {userId}에 들아갈 값 주입
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response).isNotNull();
    assertThat(response.getBody().username()).isEqualTo("민형");
  }

  @Test
  @DisplayName("사용자 삭제 통합 테스트")
  void deleteUser_e2e() {
    //given
    User user = new User("마크", "mark@test.com", "pass1234", null);
    user = userRepository.save(user);
    UUID userId = user.getId();

    //when
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/users/{userId}",
        HttpMethod.DELETE,
        null, // 보낼 Body 데이터 없음
        Void.class, // 응답 데이터가 없으므로 Void.class 사용
        userId
    );

    //then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    boolean isExist = userRepository.existsById(userId);
    assertThat(isExist).isFalse();
  }

}
