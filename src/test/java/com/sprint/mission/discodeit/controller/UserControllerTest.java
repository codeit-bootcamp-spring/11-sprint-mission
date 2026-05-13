package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  @Test
  void create_succeeds() throws Exception {
    UUID userId = UUID.randomUUID();
    UserCreateRequest request = new UserCreateRequest("testuser", "test@example.com", "password123");
    UserDto userDto = new UserDto(userId, "testuser", "test@example.com", null, false);

    MockPart requestPart = new MockPart("userCreateRequest", objectMapper.writeValueAsBytes(request));
    requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    given(userService.create(any(), any())).willReturn(userDto);

    mockMvc.perform(multipart("/api/users").part(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  void create_withDuplicateUsername_returns409() throws Exception {
    UserCreateRequest request = new UserCreateRequest("dupuser", "dup@example.com", "password123");

    MockPart requestPart = new MockPart("userCreateRequest", objectMapper.writeValueAsBytes(request));
    requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    given(userService.create(any(), any()))
        .willThrow(new UserAlreadyExistsException("username", "dupuser"));

    mockMvc.perform(multipart("/api/users").part(requestPart))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_USER"))
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  void update_succeeds() throws Exception {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newuser", "new@example.com", "newpassword1");
    UserDto updatedDto = new UserDto(userId, "newuser", "new@example.com", null, false);

    MockPart requestPart = new MockPart("userUpdateRequest", objectMapper.writeValueAsBytes(request));
    requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    given(userService.update(any(), any(), any())).willReturn(updatedDto);

    mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userId).part(requestPart))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("newuser"))
        .andExpect(jsonPath("$.email").value("new@example.com"));
  }

  @Test
  void update_withNonExistentUser_returns404() throws Exception {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("newuser", "new@example.com", "newpassword1");

    MockPart requestPart = new MockPart("userUpdateRequest", objectMapper.writeValueAsBytes(request));
    requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    given(userService.update(any(), any(), any())).willThrow(new UserNotFoundException(userId));

    mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", userId).part(requestPart))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void delete_succeeds() throws Exception {
    UUID userId = UUID.randomUUID();

    willDoNothing().given(userService).delete(userId);

    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  void delete_withNonExistentUser_returns404() throws Exception {
    UUID userId = UUID.randomUUID();

    willThrow(new UserNotFoundException(userId)).given(userService).delete(userId);

    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }

  @Test
  void findAll_succeeds() throws Exception {
    UserDto dto1 = new UserDto(UUID.randomUUID(), "user1", "user1@example.com", null, false);
    UserDto dto2 = new UserDto(UUID.randomUUID(), "user2", "user2@example.com", null, true);

    given(userService.findAll()).willReturn(List.of(dto1, dto2));

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].username").value("user1"))
        .andExpect(jsonPath("$[1].online").value(true));
  }
}