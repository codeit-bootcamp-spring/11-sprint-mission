package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Gson gson;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private MessageRepository messageRepository;

  @Test
  @DisplayName("메시지 생성 성공")
  void create_success_message() throws Exception {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    MessageCreateRequest request = new MessageCreateRequest("메시지", channel.getId(), user.getId());

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(new MockMultipartFile(
                "messageCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(request))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("메시지"));
  }

  @Test
  @DisplayName("메시지 생성 실패(채널이 존재하지 않음)")
  void create_fail_message_notfound_channel() throws Exception {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    MessageCreateRequest request = new MessageCreateRequest(
        "메시지 내용", UUID.randomUUID(), user.getId());

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(new MockMultipartFile(
                "messageCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(request))))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("메시지 수정 성공")
  void update_success_message() throws Exception {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    Message message = messageRepository.save(Message.create("메시지", channel, user));

    MessageUpdateRequest request = new MessageUpdateRequest("수정");

    // when & then
    mockMvc.perform(patch("/api/messages/{messageId}", message.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정"));
  }

  @Test
  @DisplayName("메시지 수정 실패(메시지가 존재하지 않음)")
  void update_fail_message_notfound_message() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();

    MessageUpdateRequest request = new MessageUpdateRequest("수정 메시지");

    // when & then
    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("메시지 삭제 성공")
  void delete_success_message() throws Exception {
    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    Channel channel = channelRepository.save(Channel.createPublic("채널", "설명"));

    Message message = messageRepository.save(Message.create("메시지", channel, user));

    UUID messageId = message.getId();

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("메시지 삭제 실패(메시지가 존재하지 않음)")
  void delete_fail_message_notfound_message() throws Exception {
    // given
    UUID messageId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("특정 채널의 메시지 조회 성공")
  void findAllByChannelId_success_message() throws Exception {

    // given
    User user = userRepository.save(User.create("test", "test@naver.com", "12345678"));

    Channel channel = channelRepository.save(Channel.createPublic("채널", "설명"));

    Message message1 = messageRepository.save(Message.create("메시지1", channel, user));
    Message message2 = messageRepository.save(Message.create("메시지2", channel, user));

    // when & then
    mockMvc.perform(get("/api/messages")
            .param("channelId", channel.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2));
  }

  @Test
  @DisplayName("특정 채널의 메시지 조회 실패(메시지가 존재하지 않음)")
  void findAllByChannelId_fail_message_notfound_message() throws Exception {

    // given
    UUID channelId = UUID.randomUUID();

    // when & then
    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(0));
  }
}
