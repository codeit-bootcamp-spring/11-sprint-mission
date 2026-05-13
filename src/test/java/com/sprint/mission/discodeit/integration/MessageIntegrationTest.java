package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChannelRepository channelRepository;

  private User savedUser;
  private Channel savedChannel;
  private Message savedMessage;

  @BeforeEach
  void setUp() {
    User user = new User("testUser", "test@test.com", "password123", null);
    UserStatus status = new UserStatus(user);
    user.initStatus(status);
    savedUser = userRepository.save(user);

    savedChannel = channelRepository.save(
        new Channel("public", "public Channel", ChannelType.PUBLIC)
    );

    savedMessage = messageRepository.save(
        new Message(savedChannel, savedUser, "기존 메시지")
    );
  }

  // --- POST ---
  @Test
  @DisplayName("유효한 요청으로 메시지 생성 시 201과 생성된 메시지 정보 반환")
  void createMessage_returns201() throws Exception {
    CreateMessageRequest request = new CreateMessageRequest(
        savedChannel.getId(), savedUser.getId(), "test"
    );

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("test"))
        .andExpect(jsonPath("$.channelId").value(savedChannel.getId().toString()))
        .andExpect(jsonPath("$.id").isNotEmpty());
  }

  @Test
  @DisplayName("존재하지 않는 채널 ID로 메시지 생성 시 404를 반환")
  void createMessage_channelNotFound_returns404() throws Exception {
    CreateMessageRequest request = new CreateMessageRequest(
        UUID.randomUUID(), savedUser.getId(), "test"
    );

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
  }

  // --- GET ---
  @Test
  @DisplayName("channelId로 메시지 조회 시 200과 메시지 목록을 반환")
  void readMessage_returns200WithMessageList() throws Exception {
    mockMvc.perform(get("/api/messages").param("channelId", savedChannel.getId().toString()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].content").value("기존 메시지"))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  @DisplayName("메시지가 없는 채널 조회 시 200과 빈 content를 반환")
  void readMessage_emptyChannel_returnsEmptyContent() throws Exception {
    Channel emptyChannel = channelRepository.save(
        new Channel("empty", "empty Channel", ChannelType.PUBLIC)
    );

    mockMvc.perform(get("/api/messages").param("channelId", emptyChannel.getId().toString()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  // --- DELETE ---
  @Test
  @DisplayName("메시지 삭제 시 204를 반환하고 DB에서 삭제됨")
  void deleteMessage_returns204AndDeletedFromDb() throws Exception {
    mockMvc.perform(delete("/api/messages/{messageId}", savedMessage.getId()))
        .andDo(print())
        .andExpect(status().isNoContent());

    assertThat(messageRepository.findById(savedMessage.getId())).isEmpty();
  }

  @Test
  @DisplayName("존재하지 않는 메시지 삭제 시 404를 반환")
  void deleteMessage_notFound_returns404() throws Exception {
    mockMvc.perform(delete("/api/messages/{messageId}", UUID.randomUUID()))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
  }

  // --- PATCH ---
  @Test
  @DisplayName("유효한 요청으로 메시지 수정 시 200과 수정된 메시지 정보를 반환")
  void updateMessage_returns200() throws Exception {
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 메시지");

    mockMvc.perform(patch("/api/messages/{messageId}", savedMessage.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 메시지"))
        .andExpect(jsonPath("$.id").value(savedMessage.getId().toString()));
  }

  @Test
  @DisplayName("존재하지 않는 메시지 수정 시 404를 반환")
  void updateMessage_notFound_returns404() throws Exception {
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");

    mockMvc.perform(patch("/api/messages/{messageId}", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
  }
}
