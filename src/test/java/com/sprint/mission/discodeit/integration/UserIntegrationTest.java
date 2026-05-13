package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.repository.UserRepository;
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

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Test
  void 사용자_생성_성공() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/users").file(userPart))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("jihye"))
        .andExpect(jsonPath("$.email").value("jihye@test.com"));

    assertThat(userRepository.existsByUsername("jihye")).isTrue();
  }

  @Test
  void 사용자_생성_실패_중복username() throws Exception {
    // given
    UserCreateRequest request1 = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    MockMultipartFile userPart1 = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request1));
    mockMvc.perform(multipart("/api/users").file(userPart1));

    UserCreateRequest request2 = new UserCreateRequest("jihye", "jihye2@test.com", "password123");
    MockMultipartFile userPart2 = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request2));

    // when & then
    mockMvc.perform(multipart("/api/users").file(userPart2))
        .andExpect(status().isConflict());
  }

  @Test
  void 사용자_목록_조회_성공() throws Exception {
    // given
    UserCreateRequest request = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));
    mockMvc.perform(multipart("/api/users").file(userPart));

    // when & then
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void 사용자_수정_성공() throws Exception {
    // given - 사용자 생성
    UserCreateRequest createRequest = new UserCreateRequest("jihye", "jihye@test.com",
        "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(createRequest));
    MvcResult createResult = mockMvc.perform(multipart("/api/users").file(userPart))
        .andReturn();

    JsonNode node = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String userId = node.get("id").asText();

    // when - 수정
    UserUpdateRequest updateRequest = new UserUpdateRequest("newJihye", null, null);
    MockMultipartFile updatePart = new MockMultipartFile(
        "userUpdateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(updateRequest));

    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(updatePart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("newJihye"));
  }

  @Test
  void 사용자_삭제_성공() throws Exception {
    // given - 사용자 생성
    UserCreateRequest request = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));
    MvcResult createResult = mockMvc.perform(multipart("/api/users").file(userPart))
        .andReturn();

    JsonNode node = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String userId = node.get("id").asText();

    // when & then - 삭제만 검증
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());
  }
}