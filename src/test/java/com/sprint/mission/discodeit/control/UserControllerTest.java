package com.sprint.mission.discodeit.control;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.advice.GlobalExceptionHandler;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  // --- POST ---
  @Test
  @DisplayName("사용자 생성 시 201과 생성된 사용자 JSON 반환")
  void createUser_returns201WithBody() throws Exception {
    UserCreateRequest request = new UserCreateRequest("test", "test@test.com", "password123");
    UserDto response = new UserDto(UUID.randomUUID(), "test", "test@test.com", null, true,
        Role.USER);

    given(userService.createUser(any(), any())).willReturn(response);
    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("test"))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @DisplayName("유효성 검사 실패 시 400과 VALIDATION_ERROR 코드를 반환")
  void createUser_invalidRequest_return400() throws Exception {
    UserCreateRequest request = new UserCreateRequest("a", "test-test-email", "pass");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  // --- GET ---
  @Test
  @DisplayName("전체 사용자 조회 시 200과 사용자 목록 JSON 반환")
  void getAllUsers_success() throws Exception {
    UserDto user1 = new UserDto(UUID.randomUUID(), "test1", "test1@test.com", null, true,
        Role.USER);
    UserDto user2 = new UserDto(UUID.randomUUID(), "test2", "test2@test.com", null, false,
        Role.USER);
    given(userService.allReadUser()).willReturn(List.of(user1, user2));

    mockMvc.perform(get("/api/users"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].username").value("test1"))
        .andExpect(jsonPath("$[1].username").value("test2"));
  }

  @Test
  @DisplayName("사용자가 없을 때 전체를 조회하면 200과 빈 배열을 반환")
  void getAllUsers_empty_returnEmptyArray() throws Exception {
    given(userService.allReadUser()).willReturn(List.of());

    mockMvc.perform(get("/api/users"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // --- DELETE ---
  @Test
  @DisplayName("존재하는 사용자 삭제 요청 시 204를 반환")
  void deleteUser_success_returns204() throws Exception {
    UUID userId = UUID.randomUUID();
    willDoNothing().given(userService).deleteUser(userId);

    mockMvc.perform(delete("/api/users/{id}", userId))
        .andDo(print())
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("존재하지 않는 사용자 삭제 시 404와 USER_NOT_FOUND 반환")
  void deleteUser_notFound_return404() throws Exception {
    UUID userId = UUID.randomUUID();
    willThrow(new UserNotFoundException(userId)).given(userService).deleteUser(userId);

    mockMvc.perform(delete("/api/users/{id}", userId))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }

  // --- PATCH ---
  @Test
  @DisplayName("유효한 요청으로 사용자 수정 시 200과 수정된 사용자 JSON을 반환")
  void updateUser_return200() throws Exception {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("updatedName", null, null);
    UserDto response = new UserDto(userId, "updatedName", "test@test.com", null, true, Role.USER);
    given(userService.updateUser(eq(userId), any(), any())).willReturn(response);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/users/{id}", userId)
            .file(requestPart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("updatedName"));
  }

  @Test
  @DisplayName("유효성 검사 실패 시 400과 VALIDATION_ERROR 코드를 반환")
  void updateUser_invalidRequest_return400() throws Exception {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest invalidRequest = new UserUpdateRequest(null, null, "pass");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(invalidRequest)
    );

    mockMvc.perform(multipart("/api/users/{id}", userId)
            .file(requestPart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }
}