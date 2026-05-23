package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("사용자를 생성하고 목록에서 조회할 수 있다")
    void user_createAndFindAll_success() throws Exception {
        // given
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

        // when & then - create
        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("evan"))
                .andExpect(jsonPath("$.email").value("evan@test.com"));

        // when & then - findAll
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("evan"))
                .andExpect(jsonPath("$[0].email").value("evan@test.com"));
    }

    @Test
    @DisplayName("사용자를 수정하고 삭제할 수 있다")
    void user_updateAndDelete_success() throws Exception {
        // given
        User user = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        MockMultipartFile userUpdateRequest = new MockMultipartFile(
                "userUpdateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "newUsername": "newEvan",
                  "newEmail": "new@test.com",
                  "newPassword": "newPassword123"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        // when & then - update
        mockMvc.perform(multipart("/api/users/{userId}", user.getId())
                        .file(userUpdateRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.username").value("newEvan"))
                .andExpect(jsonPath("$.email").value("new@test.com"));

        // when & then - delete
        mockMvc.perform(delete("/api/users/{userId}", user.getId()))
                .andExpect(status().isNoContent());
    }
}