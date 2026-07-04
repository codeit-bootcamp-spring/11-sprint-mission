package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void jwtAuthenticationFlow_success() throws Exception {
        User user = userRepository.saveAndFlush(new User(
                "jwt-user",
                "jwt-user@test.com",
                passwordEncoder.encode("password123")
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .param("username", user.getUsername())
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userDto.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(
                loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("accessToken").asText();
        Cookie refreshTokenCookie = loginResult.getResponse().getCookie(
                JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                .andReturn();

        JsonNode refreshBody = objectMapper.readTree(
                refreshResult.getResponse().getContentAsString());
        String newAccessToken = refreshBody.get("accessToken").asText();
        Cookie newRefreshTokenCookie = refreshResult.getResponse().getCookie(
                JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME);

        assertThat(newAccessToken).isNotEqualTo(accessToken);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(newRefreshTokenCookie)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                        0
                ));

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(newRefreshTokenCookie)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_fail_whenRefreshTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
