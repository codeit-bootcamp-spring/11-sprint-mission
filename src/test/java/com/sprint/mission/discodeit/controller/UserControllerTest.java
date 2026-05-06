package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserStatusService userStatusService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void create_success() throws Exception {
        UUID userId = UUID.randomUUID();

        UserDto response = new UserDto(
                userId,
                "taehk23",
                "taehk23@test.com",
                null,
                true
        );

        MockMultipartFile userCreateRequest = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "username": "taehk23",
                  "email": "taehk23@test.com",
                  "password": "password"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        given(userService.create(any(), any())).willReturn(response);

        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("taehk23"))
                .andExpect(jsonPath("$.email").value("taehk23@test.com"))
                .andExpect(jsonPath("$.online").value(true));

        then(userService).should().create(any(), any());
    }

    @Test
    void create_fail_whenUsernameBlank() throws Exception {
        MockMultipartFile userCreateRequest = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "username": "",
                  "email": "taehk23@test.com",
                  "password": "password"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.exceptionType").value("ValidationError"))
                .andExpect(jsonPath("$.details.username").exists());

        then(userService).should(never()).create(any(), any());
    }

    @Test
    void findAll_success() throws Exception {
        UUID userId = UUID.randomUUID();

        UserDto user = new UserDto(
                userId,
                "taehk23",
                "taehk23@test.com",
                null,
                false
        );

        given(userService.findAll()).willReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("taehk23"))
                .andExpect(jsonPath("$[0].email").value("taehk23@test.com"))
                .andExpect(jsonPath("$[0].online").value(false));

        then(userService).should().findAll();
    }

    @Test
    void delete_success() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());

        then(userService).should().delete(userId);
    }

    @Test
    void delete_fail_whenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        willThrow(new UserNotFoundException(userId))
                .given(userService)
                .delete(userId);

        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.exceptionType").value("UserNotFoundException"));

        then(userService).should().delete(userId);
    }
}
