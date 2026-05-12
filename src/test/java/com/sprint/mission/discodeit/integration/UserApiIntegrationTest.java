package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepo;

    @Test
    void createUser_success() throws Exception {
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

        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("taehk23"))
                .andExpect(jsonPath("$.email").value("taehk23@test.com"));

        assertThat(userRepo.findByUsername("taehk23")).isPresent();
    }

    @Test
    void findAllUsers_success() throws Exception {
        userRepo.save(new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("taehk23"))
                .andExpect(jsonPath("$[0].email").value("taehk23@test.com"));
    }

    @Test
    void updateUser_success() throws Exception {
        User user = userRepo.save(new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        ));

        MockMultipartFile userUpdateRequest = new MockMultipartFile(
                "userUpdateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "newUsername": "updated",
                  "newEmail": "updated@test.com",
                  "newPassword": "newPassword"
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/users/{userId}", user.getId())
                        .file(userUpdateRequest)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());

        User updatedUser = userRepo.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("updated");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void deleteUser_success() throws Exception {
        User user = userRepo.save(new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        ));

        mockMvc.perform(delete("/api/users/{userId}", user.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepo.findById(user.getId())).isEmpty();
    }

    @Test
    void createUser_fail_whenUsernameDuplicated() throws Exception {
        userRepo.save(new User(
                "taehk23",
                "existing@test.com",
                "password",
                null
        ));

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

        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionType").value("UsernameAlreadyExistsException"));
    }
}

