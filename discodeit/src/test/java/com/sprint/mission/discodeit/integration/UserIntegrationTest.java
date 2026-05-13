package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.sprint.mission.discodeit.service.dto.user.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private MockMultipartFile userPart(CreateUserRequest request) throws Exception {
        return new MockMultipartFile(
                "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    @Test
    void 사용자_생성_성공_201() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("alice")
                .email("alice@example.com")
                .password("password1234")
                .build();

        mockMvc.perform(multipart("/api/users").file(userPart(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void 사용자_생성_중복_username_409() throws Exception {
        CreateUserRequest first = CreateUserRequest.builder()
                .username("bob")
                .email("bob1@example.com")
                .password("password1234")
                .build();
        CreateUserRequest duplicate = CreateUserRequest.builder()
                .username("bob")
                .email("bob2@example.com")
                .password("password1234")
                .build();

        mockMvc.perform(multipart("/api/users").file(userPart(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/users").file(userPart(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_USERNAME"));
    }

    @Test
    void 사용자_목록_조회_성공_200() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("carol")
                .email("carol@example.com")
                .password("password1234")
                .build();

        mockMvc.perform(multipart("/api/users").file(userPart(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.username == 'carol')]").exists());
    }

    @Test
    void 사용자_삭제_성공_204() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("dave")
                .email("dave@example.com")
                .password("password1234")
                .build();

        MvcResult result = mockMvc.perform(multipart("/api/users").file(userPart(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");

        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void 존재하지_않는_사용자_삭제_404() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
