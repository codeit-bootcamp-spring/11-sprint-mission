package com.sprint.mission.discodeit.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private UUID channelId;
  private UUID authorId;
  private DiscodeitUserDetails authorDetails;  // 추가

  @BeforeEach
  void setUp() throws Exception {
    // 사용자 생성
    UserCreateRequest userRequest = new UserCreateRequest("jihye", "jihye@test.com", "password123");
    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(userRequest));
    MvcResult userResult = mockMvc.perform(
        multipart("/api/users").file(userPart).with(csrf())
            .with(user("jihye").roles("CHANNEL_MANAGER"))).andReturn();
    JsonNode userNode = objectMapper.readTree(userResult.getResponse().getContentAsString());
    authorId = UUID.fromString(userNode.get("id").asText());

    // 작성자 DiscodeitUserDetails 생성  // 추가
    UserDto authorDto = new UserDto(authorId, "jihye", "jihye@test.com", null, false, Role.USER);
    authorDetails = new DiscodeitUserDetails(authorDto, "password123");

    // 채널 생성
    PublicChannelCreateRequest channelRequest = new PublicChannelCreateRequest("general", "공용 채널");
    MvcResult channelResult = mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(channelRequest)).with(csrf())
            .with(user("jihye").roles("CHANNEL_MANAGER")))
        .andReturn();
    JsonNode channelNode = objectMapper.readTree(channelResult.getResponse().getContentAsString());
    channelId = UUID.fromString(channelNode.get("id").asText());
  }

  @Test
  void 메시지_생성_성공() throws Exception {
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    mockMvc.perform(multipart("/api/messages").file(messagePart).with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("안녕하세요"));
  }

  @Test
  void 메시지_생성_실패_존재하지않는채널() throws Exception {
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", UUID.randomUUID(), authorId);
    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));

    mockMvc.perform(multipart("/api/messages").file(messagePart).with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)))
        .andExpect(status().isNotFound());
  }

  @Test
  void 메시지_목록_조회_성공() throws Exception {
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request));
    mockMvc.perform(multipart("/api/messages").file(messagePart).with(csrf())
        .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)));

    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString())
            .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void 메시지_수정_성공() throws Exception {
    MessageCreateRequest createRequest = new MessageCreateRequest("안녕하세요", channelId, authorId);
    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(createRequest));
    MvcResult createResult = mockMvc.perform(
            multipart("/api/messages").file(messagePart).with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)))
        .andReturn();
    JsonNode node = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String messageId = node.get("id").asText();

    MessageUpdateRequest updateRequest = new MessageUpdateRequest("수정된 내용");
    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 내용"));
  }

  @Test
  void 메시지_삭제_성공() throws Exception {
    MessageCreateRequest createRequest = new MessageCreateRequest("안녕하세요", channelId, authorId);
    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(createRequest));
    MvcResult createResult = mockMvc.perform(
            multipart("/api/messages").file(messagePart).with(csrf())
                .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)))
        .andReturn();
    JsonNode node = objectMapper.readTree(createResult.getResponse().getContentAsString());
    String messageId = node.get("id").asText();

    mockMvc.perform(delete("/api/messages/{messageId}", messageId)
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user(authorDetails)))
        .andExpect(status().isNoContent());
  }
}