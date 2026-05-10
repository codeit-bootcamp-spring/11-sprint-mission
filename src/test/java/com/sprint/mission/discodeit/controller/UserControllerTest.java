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
import com.sprint.mission.discodeit.exception.user.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    UserStatusService userStatusService;

    private UserDto userDto(String username) {
        return new UserDto(UUID.randomUUID(), username, username + "@test.com", null, false);
    }

    @Test
    void create_정상_201반환() throws Exception {
        UserCreateRequest request = new UserCreateRequest("testuser", "test@test.com", "password");
        given(userService.create(any(), any())).willReturn(userDto("testuser"));

        MockMultipartFile userPart = new MockMultipartFile(
            "userCreateRequest", "", "application/json",
            objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/users").file(userPart))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void create_유효성실패_400반환() throws Exception {
        UserCreateRequest request = new UserCreateRequest("", "test@test.com", "password");

        MockMultipartFile userPart = new MockMultipartFile(
            "userCreateRequest", "", "application/json",
            objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/users").file(userPart))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void create_이메일중복_409반환() throws Exception {
        UserCreateRequest request = new UserCreateRequest("testuser", "dup@test.com", "password");
        given(userService.create(any(), any())).willThrow(new DuplicateEmailException());

        MockMultipartFile userPart = new MockMultipartFile(
            "userCreateRequest", "", "application/json",
            objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/users").file(userPart))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("USER_EMAIL_DUPLICATE"));
    }

    @Test
    void findAll_정상_200반환() throws Exception {
        given(userService.findAll()).willReturn(List.of(userDto("user1"), userDto("user2")));

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void delete_정상_204반환() throws Exception {
        UUID userId = UUID.randomUUID();
        willDoNothing().given(userService).delete(userId);

        mockMvc.perform(delete("/api/users/{userId}", userId))
            .andExpect(status().isNoContent());
    }

    @Test
    void delete_없는유저_404반환() throws Exception {
        UUID userId = UUID.randomUUID();
        willThrow(new UserNotFoundException()).given(userService).delete(userId);

        mockMvc.perform(delete("/api/users/{userId}", userId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}