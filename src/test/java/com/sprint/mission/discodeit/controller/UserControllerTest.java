package com.sprint.mission.discodeit.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.exception.service.user.NonExistUserException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("사용자 생성 성공 - 201 응답")
  void userPostTest() throws Exception {
    //given
    UserCreateRequest request = new UserCreateRequest("test1", "test@test.com", "password1");
    UserDto response = new UserDto(UUID.randomUUID(), request.username(), request.email(), null,
        true);

    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    //when
    given(userService.create(any(UserCreateRequest.class), any())).willReturn(
        response);

    mockMvc.perform(multipart("/api/users")
            .file(userPart)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("test1"))
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }

  @Test
  @DisplayName("사용자 생성 실패 - 400 응답")
  void userPostFailByPasswordTest() throws Exception {
    //given
    UserCreateRequest request = new UserCreateRequest("test1", "test@test.com", "1");
    UserDto response = new UserDto(UUID.randomUUID(), request.username(), request.email(), null,
        true);

    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    //when
    given(userService.create(any(UserCreateRequest.class), any())).willReturn(
        response);

    mockMvc.perform(multipart("/api/users")
            .file(userPart)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

  }

  @Test
  @DisplayName("사용자 정보 요청- 200 응답")
  void userGetTest() throws Exception {
    //given

    UUID userId = UUID.randomUUID();

    UserDto response = new UserDto(userId, "test1", "test@test.com", null,
        true);

    //when
    given(userService.find(any(UUID.class))).willReturn(response);

    mockMvc.perform(get("/api/users/{id}", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("test1"))
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }

  @Test
  @DisplayName("사용자 정보 요청 실패 - 404 응답")
  void userGetFailTest() throws Exception {
    //given

    UUID userId = UUID.randomUUID();

    //when
    given(userService.find(any(UUID.class))).willThrow(new NonExistUserException(userId));

    mockMvc.perform(get("/api/users/{id}", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

  }


}
