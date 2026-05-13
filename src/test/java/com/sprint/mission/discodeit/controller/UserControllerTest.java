package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
  void 사용자_목록_조회_성공() throws Exception {
    // given
    UserDto dto = new UserDto(UUID.randomUUID(), "jihye", "jihye@test.com", null, false);
    given(userService.findAll()).willReturn(List.of(dto));

    // when & then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").value("jihye"))
        .andExpect(jsonPath("$[0].email").value("jihye@test.com"));
  }

  @Test
  void 사용자_생성_성공() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UserDto dto = new UserDto(userId, "jihye", "jihye@test.com", null, false);
    given(userService.create(any(), any())).willReturn(dto);

    UserCreateRequest request = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(userPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("jihye"))
        .andExpect(jsonPath("$.email").value("jihye@test.com"));
  }

  @Test
  void 사용자_생성_실패_유효성검사() throws Exception {
    // given - username 없음
    UserCreateRequest request = new UserCreateRequest("", "jihye@test.com", "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(userPart))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 사용자_삭제_성공() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    willDoNothing().given(userService).delete(userId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  void 사용자_삭제_실패_존재하지않는유저() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    given(userService.findAll()).willReturn(List.of());
    willDoNothing().given(userService).delete(any());

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());
  }
}