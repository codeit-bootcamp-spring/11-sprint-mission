package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class UserApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  private UUID userId;
  private String username;
  private String email;
  private String password;

  @BeforeEach
  void setUp() {
    username = "tester";
    email = "tester@example.io";
    password = "password1234";

    UserResponse user = userService.createUser(
        new UserCreateRequest(username, email, password),
        Optional.empty()
    );
    userId = user.id();
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("success")
    void create_success() throws Exception {
      // given
      UserCreateRequest request = new UserCreateRequest("newuser", "newuser@example.io",
          "password1234");
      MockMultipartFile requestPart = new MockMultipartFile(
          "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      mockMvc.perform(multipart("/api/users")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(notNullValue()))
          .andExpect(jsonPath("$.username").value("newuser"))
          .andExpect(jsonPath("$.email").value("newuser@example.io"));
    }

    @Test
    @DisplayName("fail with duplicate user")
    void create_fail_duplicate_user_throws_exception() throws Exception {
      // given - same username as setUp already exists
      UserCreateRequest request = new UserCreateRequest(username, "other@example.io",
          "password1234");
      MockMultipartFile requestPart = new MockMultipartFile(
          "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      ErrorCode errorCode = ErrorCode.DUPLICATE_USER;

      mockMvc.perform(multipart("/api/users")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(
              jsonPath("$.exceptionType").value(DuplicateUserException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("success")
    void update_success() throws Exception {
      // given
      String newUsername = "updated";
      UserUpdateRequest request = new UserUpdateRequest(newUsername, null, null);
      MockMultipartFile requestPart = new MockMultipartFile(
          "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      mockMvc.perform(multipart("/api/users/{userId}", userId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.username").value(newUsername));
    }

    @Test
    @DisplayName("fail with user not found")
    void update_fail_user_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("newName", null, null);
      MockMultipartFile requestPart = new MockMultipartFile(
          "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then
      ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

      mockMvc.perform(multipart("/api/users/{userId}", nonExistentId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.userId").value(nonExistentId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(UserNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("success")
    void delete_success() throws Exception {
      // when & then
      mockMvc.perform(delete("/api/users/{userId}", userId))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("fail with user not found")
    void delete_fail_user_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();

      // when & then
      ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

      mockMvc.perform(delete("/api/users/{userId}", nonExistentId))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(
              jsonPath("$.exceptionType").value(UserNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAll")
  class FindAll {

    @Test
    @DisplayName("success")
    void findAll_success() throws Exception {
      // when & then
      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].id").value(userId.toString()))
          .andExpect(jsonPath("$[0].username").value(username));
    }
  }

  @Nested
  @DisplayName("updateUserStatus")
  class UpdateUserStatus {

    @Test
    @DisplayName("success")
    void updateUserStatus_success() throws Exception {
      // given
      UserStatusUpdateRequest request = new UserStatusUpdateRequest(Instant.now());

      // when & then
      mockMvc.perform(patch("/api/users/{userId}/user-status", userId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(notNullValue()))
          .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    @DisplayName("fail with user not found")
    void updateUserStatus_fail_user_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();
      UserStatusUpdateRequest request = new UserStatusUpdateRequest(Instant.now());

      // when & then
      ErrorCode errorCode = ErrorCode.USER_STATUS_NOT_FOUND;

      mockMvc.perform(patch("/api/users/{userId}/user-status", nonExistentId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(
              jsonPath("$.exceptionType").value(UserStatusNotFoundException.class.getSimpleName()));
    }
  }
}