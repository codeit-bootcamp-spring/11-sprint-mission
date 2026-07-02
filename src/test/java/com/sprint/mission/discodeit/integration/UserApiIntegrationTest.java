package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.UserService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser
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
  private DiscodeitUserDetails testerDetails;

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
    testerDetails = new DiscodeitUserDetails(user, password);
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
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(csrf()))
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
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(csrf()))
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
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.username").value(newUsername));
    }

    @Test
    @DisplayName("fail: access denied when updating another user")
    void update_fail_forbidden_when_updating_another_user() throws Exception {
      // given - trying to update a different user's profile
      UUID otherUserId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest("newName", null, null);
      MockMultipartFile requestPart = new MockMultipartFile(
          "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      // when & then - ownership check fails before reaching service logic
      ErrorCode errorCode = ErrorCode.FORBIDDEN;

      mockMvc.perform(multipart("/api/users/{userId}", otherUserId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("success")
    void delete_success() throws Exception {
      // when & then
      mockMvc.perform(delete("/api/users/{userId}", userId)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("fail: access denied when deleting another user")
    void delete_fail_forbidden_when_deleting_another_user() throws Exception {
      // given - trying to delete a different user's account
      UUID otherUserId = UUID.randomUUID();

      // when & then - ownership check fails before reaching service logic
      ErrorCode errorCode = ErrorCode.FORBIDDEN;

      mockMvc.perform(delete("/api/users/{userId}", otherUserId)
              .with(user(testerDetails))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()));
    }
  }

  @Nested
  @DisplayName("findAll")
  class FindAll {

    @Test
    @DisplayName("success")
    void findAll_success() throws Exception {
      // when & then
      mockMvc.perform(get("/api/users")
              .with(user(testerDetails)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[*].id").value(hasItem(userId.toString())))
          .andExpect(jsonPath("$[*].username").value(hasItem(username)));
    }
  }
}
