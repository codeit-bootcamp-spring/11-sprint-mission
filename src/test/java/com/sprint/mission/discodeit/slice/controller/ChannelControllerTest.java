package com.sprint.mission.discodeit.slice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.sprint.mission.discodeit.controller.ChannelController;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
public class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private Gson gson;

  @MockitoBean
  private ChannelService channelService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;


  @Test
  @DisplayName("공개 채널 생성 성공")
  void createPublic_success() throws Exception {
    // given
    ChannelCreatePublicRequest request = new ChannelCreatePublicRequest("공개", "공개 채널입니다.");
    Channel channel = Channel.createPublic("공개", "공개 채널입니다.");
    ChannelDto dto = new ChannelDto(
        channel.getId(), ChannelType.PUBLIC, "공개", "공개 채널입니다", List.of(), Instant.now()
    );

    given(channelService.createPublic(any())).willReturn(dto);

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("공개"));
  }

  @Test
  @DisplayName("비공개 채널 생성 성공")
  void createPrivate_success() throws Exception {
    // given
    User user1 = User.create("test1", "test1@naver.com", "1234");
    User user2 = User.create("test2", "test2@naver.com", "1234");
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();

    UserDto userDto1 = new UserDto(
        user1.getId(), user1.getUsername(), user1.getEmail(), null, true
    );
    UserDto userDto2 = new UserDto(
        user2.getId(), user2.getUsername(), user2.getEmail(), null, true
    );

    ChannelCreatePrivateRequest request = new ChannelCreatePrivateRequest(
        List.of(userId1, userId2));

    Channel channel = Channel.createPrivate();
    ChannelDto dto = new ChannelDto(
        channel.getId(), ChannelType.PRIVATE, null, null, List.of(userDto1, userDto2), Instant.now()
    );

    given(channelService.createPrivate(any())).willReturn(dto);

    // when & then
    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("비공개 채널 생성 실패(채널에 참여자가 없음)")
  void createPrivate_fail_channel_emptyParticipantIds() throws Exception {
    // given
    ChannelCreatePrivateRequest request = new ChannelCreatePrivateRequest(List.of());

    Channel channel = Channel.createPrivate();
    ChannelDto dto = new ChannelDto(
        channel.getId(), ChannelType.PRIVATE, null, null, List.of(), Instant.now()
    );

    given(channelService.createPrivate(any())).willReturn(dto);

    // when & then
    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("채널 수정 성공")
  void update_success_channel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    ChannelUpdateRequest request = new ChannelUpdateRequest("수정", null);

    Channel channel = Channel.createPublic(request.newName(), "공개 채널입니다.");
    ChannelDto dto = new ChannelDto(
        channel.getId(), ChannelType.PUBLIC, request.newName(), "공개 채널입니다", List.of(), Instant.now()
    );

    given(channelService.update(any(), any())).willReturn(dto);

    // when & then
    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("수정"));
  }

  @Test
  @DisplayName("채널 수정 실패(채널이 존재하지 않음)")
  void update_fail_channel_notfound_channel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    ChannelUpdateRequest request = new ChannelUpdateRequest("수정", null);
    given(channelService.update(any(), any())).willThrow(new ChannelNotFoundException(channelId));

    // when & then
    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(gson.toJson(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("채널 삭제 성공")
  void delete_success_channel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("채널 삭제 실패(채널이 존재하지 않음)")
  void delete_fail_channel_notfound_channel() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    willThrow(new ChannelNotFoundException(channelId)).given(channelService).delete(channelId);

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andExpect(status().isNotFound());
  }
}
