package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Gson gson;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("사용자 생성 성공")
  void create_success() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest("test", "test@naver.com", "12345678");

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(new MockMultipartFile(
                "userCreateRequest",
                "",
                "application/json",
                gson.toJson(request).getBytes())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("test"))
        .andExpect(jsonPath("$.email").value("test@naver.com"));
  }

  @Test
  @DisplayName("사용자 생성 실패(사용자 이름 중복)")
  void create_fail_duplicate_Username() throws Exception {
    // given
    UserCreateRequest request1 = new UserCreateRequest("test", "test@naver.com", "12345678");

    mockMvc.perform(multipart("/api/users")
            .file(new MockMultipartFile(
                "userCreateRequest",
                "",
                "application/json",
                gson.toJson(request1).getBytes())))
        .andExpect(status().isCreated());

    UserCreateRequest request2 = new UserCreateRequest("test", "tests@naver.com", "12345678");

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(new MockMultipartFile(
                "userCreateRequest",
                "",
                "application/json",
                gson.toJson(request2).getBytes())))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("사용자 생성 실패(Email 중복)")
  void create_fail_duplicate_Email() throws Exception {
    // given
    UserCreateRequest request1 = new UserCreateRequest("test1", "test@naver.com", "12345678");

    mockMvc.perform(multipart("/api/users")
            .file(new MockMultipartFile(
                "userCreateRequest",
                "",
                "application/json",
                gson.toJson(request1).getBytes())))
        .andExpect(status().isCreated());

    UserCreateRequest request2 = new UserCreateRequest("test2", "test@naver.com", "12345678");

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(new MockMultipartFile(
                "userCreateRequest",
                "",
                "application/json",
                gson.toJson(request2).getBytes())))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("유저 수정 성공")
  void update_success() throws Exception {
    // given
    User savedUser = userRepository.save(User.create("test", "test@naver.com", "12345678"));
    UUID userId = savedUser.getId();

    UserUpdateRequest updateRequest = new UserUpdateRequest("testA", null, null);

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(new MockMultipartFile(
                "userUpdateRequest",
                "",
                "application/json",
                gson.toJson(updateRequest).getBytes()))
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("testA"));
  }

  @Test
  @DisplayName("유저 수정 실패(유저가 존재하지 않음)")
  void update_fail() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    UserUpdateRequest updateRequest = new UserUpdateRequest("testA", null, null);

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(new MockMultipartFile(
                "userUpdateRequest",
                "",
                "application/json",
                gson.toJson(updateRequest).getBytes()))
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유저 삭제 성공")
  void delete_success() throws Exception {
    // given
    User savedUser = userRepository.save(User.create("test", "test@naver.com", "12345678"));
    UUID userId = savedUser.getId();

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("유저 삭제 실패(유저가 존재하지 않음")
  void delete_fail() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유저 조회 성공")
  void findAll_success() throws Exception {
    // given
    User user1 = userRepository.save(User.create("test1", "test1@naver.com", "12345678"));
    User user2 = userRepository.save(User.create("test2", "test2@naver.com", "12345678"));

    // when & then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("유저 조회 실패(유저가 존재하지 않음")
  void findAll_fail() throws Exception {
    // given : List 크기가 0을 유도하도록 User 생성X

    // when & then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

}
