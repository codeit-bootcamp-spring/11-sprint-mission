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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@WithMockUser(roles = {"USER", "ADMIN"})
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  // create 테스트
  @Test
  @DisplayName("올바른 사용자 정보로 생성 요청 - 201 Created 반환")
  void create_validRequest_returnsCreatedUser() throws Exception {
    // Given
    UserDto.CreateRequest request = UserDto.CreateRequest.builder()
        .username("testuser")
        .email("test@example.com")
        .password("Password123!")
        .build();

    MockMultipartFile userCreateRequestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test-image".getBytes()
    );

    UUID userId = UUID.randomUUID();
    BinaryContentDto.Response profileDto = BinaryContentDto.Response.builder()
        .id(UUID.randomUUID())
        .fileName("profile.jpg")
        .size(12L)
        .contentType(MediaType.IMAGE_JPEG_VALUE)
        .build();

    UserDto.Response createdUser = UserDto.Response.builder()
        .id(userId)
        .username("testuser")
        .email("test@example.com")
        .profile(profileDto)
        .online(false)
        .build();

    given(userService.create(any(UserDto.CreateRequest.class), any())).willReturn(createdUser);

    // When & Then
    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequestPart)
            .file(profilePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.profile.fileName").value("profile.jpg"))
        .andExpect(jsonPath("$.online").value(false));
  }

  @Test
  @DisplayName("유효하지 않은 정보로 생성 요청 - 400 Bad Request 반환")
  void create_invalidRequest_returnsBadRequest() throws Exception {
    // Given
    UserDto.CreateRequest invalidRequest = UserDto.CreateRequest.builder()
        .username("")
        .email("invalid-email")
        .password("short")
        .build();

    MockMultipartFile userCreateRequestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(invalidRequest)
    );

    // When & Then
    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequestPart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("COMMON-002"))
        .andExpect(jsonPath("$.details.username").exists())
        .andExpect(jsonPath("$.details.email").exists())
        .andExpect(jsonPath("$.details.password").exists());
  }

  @Test
  @DisplayName("지원하지 않는 미디어 타입 요청 - 415 Unsupported Media Type 반환")
  void create_unsupportedMediaType_returnsError() throws Exception {
    // When & Then
    mockMvc.perform(post("/api/users")
            .with(csrf())
            .contentType(MediaType.APPLICATION_XML_VALUE)
            .content("<xml>test</xml>"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.code").value("COMMON-007"))
        .andExpect(jsonPath("$.message").value("지원하지 않는 Content-Type입니다."));
  }

  // update 테스트
  @Test
  @DisplayName("올바른 사용자 정보로 수정 요청 - 200 OK 반환")
  void update_validRequest_returnsUpdatedUser() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    UserDto.UpdateRequest updateRequest = new UserDto.UpdateRequest(
        "updateduser",
        "updated@example.com",
        "UpdatedPassword1!"
    );

    MockMultipartFile userUpdateRequestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(updateRequest)
    );

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "updated-profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "updated-image".getBytes()
    );

    BinaryContentDto.Response profileDto = BinaryContentDto.Response.builder()
        .id(UUID.randomUUID())
        .fileName("updated-profile.jpg")
        .size(14L)
        .contentType(MediaType.IMAGE_JPEG_VALUE)
        .build();

    UserDto.Response updatedUser = UserDto.Response.builder()
        .id(userId)
        .username("updateduser")
        .email("updated@example.com")
        .profile(profileDto)
        .online(true)
        .build();

    given(userService.update(eq(userId), any(UserDto.UpdateRequest.class), any())).willReturn(
        updatedUser);

    // When & Then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(userUpdateRequestPart)
            .file(profilePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("updateduser"))
        .andExpect(jsonPath("$.email").value("updated@example.com"))
        .andExpect(jsonPath("$.profile.fileName").value("updated-profile.jpg"))
        .andExpect(jsonPath("$.online").value(true));
  }

  @Test
  @DisplayName("존재하지 않는 사용자 수정 요청 - 404 Not Found 반환")
  void update_nonExistingUser_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentUserId = UUID.randomUUID();
    UserDto.UpdateRequest updateRequest = new UserDto.UpdateRequest(
        "updateduser",
        "updated@example.com",
        "UpdatedPassword1!"
    );

    MockMultipartFile userUpdateRequestPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(updateRequest)
    );

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "updated-profile.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "updated-image".getBytes()
    );

    given(userService.update(eq(nonExistentUserId), any(UserDto.UpdateRequest.class), any()))
        .willThrow(UserNotFoundException.withId(nonExistentUserId));

    // When & Then
    mockMvc.perform(multipart("/api/users/{userId}", nonExistentUserId)
            .file(userUpdateRequestPart)
            .file(profilePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER-001"))
        .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다."));
  }

  // delete 테스트
  @Test
  @DisplayName("사용자 삭제 요청 - 204 No Content 반환")
  void delete_existingUser_returnsNoContent() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    willDoNothing().given(userService).delete(userId);

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", userId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("존재하지 않는 사용자 삭제 요청 - 404 Not Found 반환")
  void delete_nonExistingUser_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentUserId = UUID.randomUUID();
    willThrow(UserNotFoundException.withId(nonExistentUserId))
        .given(userService).delete(nonExistentUserId);

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", nonExistentUserId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER-001"))
        .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다."));
  }

  // findAll 테스트
  @Test
  @DisplayName("사용자 전체 조회 요청 - 200 OK 반환")
  void findAll_existingUsers_returnsUserList() throws Exception {
    // Given
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();

    UserDto.Response user1 = UserDto.Response.builder()
        .id(userId1)
        .username("user1")
        .email("user1@example.com")
        .profile(null)
        .online(true)
        .build();

    UserDto.Response user2 = UserDto.Response.builder()
        .id(userId2)
        .username("user2")
        .email("user2@example.com")
        .profile(null)
        .online(false)
        .build();

    List<UserDto.Response> users = List.of(user1, user2);

    given(userService.findAll()).willReturn(users);

    // When & Then
    mockMvc.perform(get("/api/users")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(userId1.toString()))
        .andExpect(jsonPath("$[0].username").value("user1"))
        .andExpect(jsonPath("$[0].online").value(true))
        .andExpect(jsonPath("$[1].id").value(userId2.toString()))
        .andExpect(jsonPath("$[1].username").value("user2"))
        .andExpect(jsonPath("$[1].online").value(false));
  }
}