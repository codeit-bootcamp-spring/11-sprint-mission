package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    User user = new User("test", "test@test.com", "password123", null);
    UserStatus status = new UserStatus(user);
    user.initStatus(status);
    savedUser = userRepository.save(user);
  }

  // --- POST ---
  @Test
  @DisplayName("유효한 요청으로 사용자 생성 시 201과 생성된 사용자 정보를 반환")
  void createUser_validRequest_returns201() throws Exception {
    UserCreateRequest request = new UserCreateRequest("newUser", "new@test.com", "password123");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andDo(print())
        .andExpect(jsonPath("$.username").value("newUser"))
        .andExpect(jsonPath("$.email").value("new@test.com"))
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @DisplayName("중복된 이메일로 사용자 생성 시 409를 반환")
  void createUser_duplicateEmail_returns409() throws Exception {
    UserCreateRequest request = new UserCreateRequest("otherUser", "test@test.com",
        "password123");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("EMAIL_EXISTS"));
  }

  // --- GET ---
  @Test
  @DisplayName("전체 사용자 조회 시 200과 사용자 목록을 반환")
  void getAllUsers_returns200WithUserList() throws Exception {
    mockMvc.perform(get("/api/users"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].username").value("test"))
        .andExpect(jsonPath("$[0].email").value("test@test.com"));
  }

  @Test
  @DisplayName("사용자가 없을 때 전체 조회를 할 경우 200과 빈 배열을 반환")
  void getAllUsers_noUsers_return200WithEmptyList() throws Exception {
    userRepository.deleteAll();

    mockMvc.perform(get("/api/users"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // --- DELETE ---
  @Test
  @DisplayName("존재하는 사용자를 삭제 할 경우 204를 반환하고 DB에서도 삭제됨")
  void deleteUser_return204AndDeletedFromDb() throws Exception {
    mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
        .andDo(print())
        .andExpect(status().isNoContent());

    assertThat(userRepository.findById(savedUser.getId())).isEmpty();
  }

  @Test
  @DisplayName("존재하지 않는 사용자를 삭제 할 경우 404를 반환")
  void deleteUser_notFound_returns404() throws Exception {
    mockMvc.perform(delete("/api/users/{id}", UUID.randomUUID()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }

  // --- PATCH ---
  @Test
  @DisplayName("유효한 요청으로 사용자를 수정 할 경우 200과 수정된 사용자 정보를 반환")
  void updateUser_returns200() throws Exception {
    UserUpdateRequest request = new UserUpdateRequest("updateUser", null, null);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users/{id}", savedUser.getId())
            .file(requestPart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("updateUser"))
        .andExpect(jsonPath("$.id").value(savedUser.getId().toString()));
  }

  @Test
  @DisplayName("존재하지 않는 사용자를 수정 할 경우 404를 반환")
  void updateUser_notFound_returns404() throws Exception {
    UserUpdateRequest request = new UserUpdateRequest("updateUser", null, null);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users/{id}", UUID.randomUUID())
            .file(requestPart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }
}
