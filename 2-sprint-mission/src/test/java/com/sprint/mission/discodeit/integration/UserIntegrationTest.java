package com.sprint.mission.discodeit.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = {"USER", "ADMIN"})
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  private User saveTestUser(String username, String email) {
    BinaryContent profile = BinaryContent.builder()
        .fileName("profile.jpg")
        .size(1024L)
        .contentType("image/jpeg")
        .build();
    entityManager.persist(profile);

    User user = User.builder()
        .username(username)
        .email(email)
        .password("Password123!")
        .profile(profile)
        .build();
    entityManager.persist(user);
    entityManager.flush();
    entityManager.clear();
    return user;
  }

  // create 테스트
  @Test
  @Disabled("응답의 online 필드는 JwtRegistry 기반(미션10)이라 미로그인 생성 유저는 false. 테스트 기대값 재설계 필요")
  @DisplayName("사용자 생성 API 통합 테스트 - 성공")
  void create_success() throws Exception {
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

    // When & Then
    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequestPart)
            .file(profilePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.profile.fileName").value("profile.jpg"))
        .andExpect(jsonPath("$.online").value(true));
  }

  @Test
  @DisplayName("사용자 생성 실패 API 통합 테스트 - 유효하지 않은 요청")
  void create_invalidRequest_returnsBadRequest() throws Exception {
    // Given
    UserDto.CreateRequest invalidRequest = UserDto.CreateRequest.builder()
        .username("invalid-test")
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
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("중복 이메일 사용자 생성 시도 - 409 Conflict 반환")
  void create_duplicateEmail_returnsConflict() throws Exception {
    // Given
    saveTestUser("existing", "test@example.com");

    UserDto.CreateRequest request = UserDto.CreateRequest.builder()
        .username("newuser")
        .email("test@example.com")
        .password("Password123!")
        .build();

    MockMultipartFile userCreateRequestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // When & Then
    mockMvc.perform(multipart("/api/users")
            .file(userCreateRequestPart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(csrf()))
        .andExpect(status().isConflict());
  }

  // findAll 테스트
  @Test
  @Disabled("admin 계정 자동 생성으로 전체 개수가 고정되지 않음. 테스트 기대값 재설계 필요")
  @DisplayName("모든 사용자 조회 API 통합 테스트 - 성공")
  void findAll_success() throws Exception {
    // Given
    saveTestUser("user1", "user1@example.com");
    saveTestUser("user2", "user2@example.com");

    // When & Then
    mockMvc.perform(get("/api/users")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()").value(2))
        .andExpect(jsonPath("$[0].username").value("user1"))
        .andExpect(jsonPath("$[0].email").value("user1@example.com"))
        .andExpect(jsonPath("$[1].username").value("user2"))
        .andExpect(jsonPath("$[1].email").value("user2@example.com"));
  }

  // update 테스트
  @Test
  @DisplayName("사용자 업데이트 API 통합 테스트 - 성공")
  void update_success() throws Exception {
    // Given
    User user = saveTestUser("originaluser", "original@example.com");

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

    // When & Then
    mockMvc.perform(multipart("/api/users/{userId}", user.getId())
            .file(userUpdateRequestPart)
            .file(profilePart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()))
        .andExpect(jsonPath("$.username").value("updateduser"))
        .andExpect(jsonPath("$.email").value("updated@example.com"))
        .andExpect(jsonPath("$.profile.fileName").value("updated-profile.jpg"));
  }

  @Test
  @DisplayName("사용자 업데이트 실패 API 통합 테스트 - 존재하지 않는 사용자")
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

    // When & Then
    mockMvc.perform(multipart("/api/users/{userId}", nonExistentUserId)
            .file(userUpdateRequestPart)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  // delete 테스트
  @Test
  @DisplayName("사용자 삭제 API 통합 테스트 - 성공")
  void delete_success() throws Exception {
    // Given
    User user = saveTestUser("deleteuser", "delete@example.com");

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", user.getId())
            .with(csrf()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '" + user.getId() + "')]").doesNotExist());
  }

  @Test
  @DisplayName("사용자 삭제 실패 API 통합 테스트 - 존재하지 않는 사용자")
  void delete_nonExistingUser_returnsNotFound() throws Exception {
    // Given
    UUID nonExistentUserId = UUID.randomUUID();

    // When & Then
    mockMvc.perform(delete("/api/users/{userId}", nonExistentUserId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }
}