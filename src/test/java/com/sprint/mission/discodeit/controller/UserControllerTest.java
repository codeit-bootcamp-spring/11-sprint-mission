package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@WithMockUser
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  private UUID userId;
  private String username;
  private String email;
  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    username = "tester";
    email = "tester@example.io";
    userResponse = new UserResponse(userId, username, email, null, true, Role.USER);
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("success")
    void create_success() throws Exception {
      // given
      UserCreateRequest request = new UserCreateRequest(username, email, "password1234");
      MockMultipartFile requestPart = new MockMultipartFile(
          "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      given(userService.createUser(any(), any())).willReturn(userResponse);

      // when & then
      mockMvc.perform(multipart("/api/users")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.username").value(username))
          .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("fail with duplicate user")
    void create_fail_duplicate_user_throws_exception() throws Exception {
      // given
      UserCreateRequest request = new UserCreateRequest(username, email, "password1234");
      MockMultipartFile requestPart = new MockMultipartFile(
          "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      given(userService.createUser(any(), any()))
          .willThrow(DuplicateUserException.withUsername(username));

      // when & then
      ErrorCode errorCode = ErrorCode.DUPLICATE_USER;

      mockMvc.perform(multipart("/api/users")
              .file(requestPart)
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.username").value(username))
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
      UserResponse updatedResponse = new UserResponse(userId, newUsername, email, null, true, Role.USER);

      given(userService.updateUser(eq(userId), any(), any()))
          .willReturn(updatedResponse);

      // when & then
      mockMvc.perform(multipart("/api/users/{userId}", userId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(userId.toString()))
          .andExpect(jsonPath("$.username").value(newUsername));
    }

    @Test
    @DisplayName("fail with user not found")
    void update_fail_user_not_found_throws_exception() throws Exception {
      // given
      UserUpdateRequest request = new UserUpdateRequest("newName", null, null);
      MockMultipartFile requestPart = new MockMultipartFile(
          "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
          objectMapper.writeValueAsBytes(request));

      given(userService.updateUser(eq(userId), any(), any()))
          .willThrow(UserNotFoundException.withId(userId));

      // when & then
      ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

      mockMvc.perform(multipart("/api/users/{userId}", userId)
              .file(requestPart)
              .with(req -> {
                req.setMethod("PATCH");
                return req;
              })
              .contentType(MediaType.MULTIPART_FORM_DATA)
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.userId").value(userId.toString()))
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
      // given
      willDoNothing().given(userService).deleteUser(userId);

      // when & then
      mockMvc.perform(delete("/api/users/{userId}", userId)
              .with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("fail with user not found")
    void delete_fail_user_not_found_throws_exception() throws Exception {
      // given
      willThrow(UserNotFoundException.withId(userId))
          .given(userService).deleteUser(userId);

      // when & then
      ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

      mockMvc.perform(delete("/api/users/{userId}", userId)
              .with(csrf()))
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
      // given
      List<UserResponse> responses = List.of(
          userResponse,
          new UserResponse(UUID.randomUUID(), "other", "other@example.io", null, false, Role.USER)
      );
      given(userService.findAll()).willReturn(responses);

      // when & then
      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].id").value(userId.toString()))
          .andExpect(jsonPath("$[0].username").value(username));
    }
  }

}
