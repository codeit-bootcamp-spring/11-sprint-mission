package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import java.util.List;
import java.util.UUID;
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
class ChannelIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private String createUser(String username, String email) throws Exception {
    UserCreateRequest request = new UserCreateRequest(username, email, "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));
    MvcResult result = mockMvc.perform(multipart("/api/users").file(userPart)).andReturn();
    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
    return node.get("id").asText();
  }

  @Test
  void 공개채널_생성_성공() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("general", "공용 채널");

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("general"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));
  }

  @Test
  void 공개채널_생성_실패_이름없음() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("", "공용 채널");

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 비공개채널_생성_성공() throws Exception {
    // given
    String userId1 = createUser("user1", "user1@test.com");
    String userId2 = createUser("user2", "user2@test.com");
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        List.of(UUID.fromString(userId1), UUID.fromString(userId2)));

    // when & then
    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PRIVATE"));
  }

  @Test
  void 채널_수정_성공() throws Exception {
    // given - 채널 생성
    PublicChannelCreateRequest createRequest = new PublicChannelCreateRequest("general", "공용 채널");
    MvcResult createResult = mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
        .andReturn();
    JsonNode node = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String channelId = node.get("id").asText();

    // when - 수정
    PublicChannelUpdateRequest updateRequest = new PublicChannelUpdateRequest("newName", "새 설명");
    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("newName"));
  }

  @Test
  void 채널_삭제_성공() throws Exception {
    // given - 채널 생성
    PublicChannelCreateRequest createRequest = new PublicChannelCreateRequest("general", "공용 채널");
    MvcResult createResult = mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
        .andReturn();
    JsonNode node = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String channelId = node.get("id").asText();

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andExpect(status().isNoContent());
  }

  @Test
  void 채널_목록_조회_성공() throws Exception {
    // given
    String userId = createUser("jihye", "jihye@test.com");
    mockMvc.perform(post("/api/channels/public")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(
            new PublicChannelCreateRequest("general", "공용 채널"))));

    // when & then
    mockMvc.perform(get("/api/channels")
            .param("userId", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }
}