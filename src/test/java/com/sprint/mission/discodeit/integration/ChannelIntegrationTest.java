package com.sprint.mission.discodeit.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ChannelIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Gson gson;

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("공개 채널 생성 성공")
  void createPublic_success() throws Exception {
    // given
    ChannelCreatePublicRequest request = new ChannelCreatePublicRequest("공개", "공개 채널입니다.");

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request))
            .with(user("manager").roles("CHANNEL_MANAGER"))
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PUBLIC"))
        .andExpect(jsonPath("$.name").value("공개"))
        .andExpect(jsonPath("$.description").value("공개 채널입니다."));
  }

  @Test
  @DisplayName("비공개 채널 생성 성공")
  void createPrivate_success_channel() throws Exception {
    // given
    User user1 = userRepository.save(User.create("test1", "test1@naver.com", "1234"));
    User user2 = userRepository.save(User.create("test2", "test2@naver.com", "1234"));

    ChannelCreatePrivateRequest request =
        new ChannelCreatePrivateRequest(List.of(user1.getId(), user2.getId()));

    // when & then
    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request))
            .with(user("test").roles("USER"))
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PRIVATE"));
  }

  @Test
  @DisplayName("채널 수정 성공")
  void update_success_channel() throws Exception {
    // given
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    UUID channelId = channel.getId();

    ChannelUpdateRequest request = new ChannelUpdateRequest("수정", null);

    // when & then
    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request))
            .with(user("manager").roles("CHANNEL_MANAGER"))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("수정"));
  }

  @Test
  @DisplayName("채널 수정 실패(채널이 존재하지 않음)")
  void update_fail_channel_notfound_channel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    ChannelUpdateRequest request = new ChannelUpdateRequest("수정", null);

    // when & then
    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request))
            .with(user("manager").roles("CHANNEL_MANAGER"))
            .with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("채널 삭제 성공")
  void delete_success_channel() throws Exception {
    // given
    Channel channel = channelRepository.save(Channel.createPublic("공개", "공개 채널입니다."));

    UUID channelId = channel.getId();

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId)
            .with(user("manager").roles("CHANNEL_MANAGER"))
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("채널 삭제 실패(채널이 존재하지 않음)")
  void delete_fail_channel_notfound_channel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId)
            .with(user("manager").roles("CHANNEL_MANAGER"))
            .with(csrf()))
        .andExpect(status().isNotFound());
  }
}
