package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    UserStatusService userStatusService;

    @Test
    @DisplayName("사용자 목록을 조회할 수 있다")
    void findAll_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto userDto = new UserDto(
                userId,
                "evan",
                "evan@test.com",
                null,
                true
        );

        given(userService.findAll()).willReturn(List.of(userDto));

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("evan"))
                .andExpect(jsonPath("$[0].email").value("evan@test.com"))
                .andExpect(jsonPath("$[0].online").value(true));

        then(userService).should().findAll();
    }

    @Test
    @DisplayName("multipart 요청으로 사용자를 생성할 수 있다")
    void create_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto response = new UserDto(
                userId,
                "evan",
                "evan@test.com",
                null,
                true
        );

        given(userService.create(any())).willReturn(response);

        MockMultipartFile userCreateRequest = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "username": "evan",
                  "email": "evan@test.com",
                  "password": "password123"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("evan"))
                .andExpect(jsonPath("$.email").value("evan@test.com"))
                .andExpect(jsonPath("$.online").value(true));

        then(userService).should().create(any());
    }

    @Test
    @DisplayName("사용자 생성 시 username이 비어 있으면 400을 반환한다")
    void create_fail_whenUsernameBlank() throws Exception {
        // given
        MockMultipartFile userCreateRequest = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "username": "",
                  "email": "evan@test.com",
                  "password": "password123"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.username").exists());

        then(userService).should(never()).create(any());
    }

    @Test
    @DisplayName("사용자 상태를 수정할 수 있다")
    void updateStatus_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID userStatusId = UUID.randomUUID();
        Instant lastActiveAt = Instant.parse("2026-05-09T10:00:00Z");

        UserStatusDto response = new UserStatusDto(
                userStatusId,
                userId,
                lastActiveAt
        );

        given(userStatusService.updateByUserId(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newLastActiveAt": "2026-05-09T10:00:00Z"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userStatusId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.lastActiveAt").value("2026-05-09T10:00:00Z"));

        then(userStatusService).should().updateByUserId(any(), any());
    }
}