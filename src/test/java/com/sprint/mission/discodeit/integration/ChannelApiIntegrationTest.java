package com.sprint.mission.discodeit.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.DuplicateChannelException;
import com.sprint.mission.discodeit.exception.channel.NoValidParticipantsException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateForbiddenException;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@WithMockUser(roles = {"CHANNEL_MANAGER"})
class ChannelApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private ChannelService channelService;

  private UUID userId;
  private UUID channelId;
  private String channelName;

  @BeforeEach
  void setUp() {
    UserResponse user = userService.createUser(
        new UserCreateRequest("tester", "tester@example.io", "password1234"),
        Optional.empty()
    );
    userId = user.id();

    channelName = "general";
    ChannelResponse channel = channelService.createPublicChannel(
        new PublicChannelCreateRequest(channelName, "description")
    );
    channelId = channel.id();
  }

  @Nested
  @DisplayName("createPublic")
  class CreatePublic {

    @Test
    @DisplayName("success")
    void createPublic_success() throws Exception {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("new-channel",
          "new description");

      // when & then
      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.type").value(ChannelType.PUBLIC.name()))
          .andExpect(jsonPath("$.name").value("new-channel"));
    }

    @Test
    @DisplayName("fail with duplicate channel")
    void createPublic_fail_duplicate_channel_throws_exception() throws Exception {
      // given - same channel name already exists from setUp
      PublicChannelCreateRequest request = new PublicChannelCreateRequest(channelName, null);

      // when & then
      ErrorCode errorCode = ErrorCode.DUPLICATE_CHANNEL;

      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.name").value(channelName))
          .andExpect(
              jsonPath("$.exceptionType").value(DuplicateChannelException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("createPrivate")
  class CreatePrivate {

    @Test
    @DisplayName("success")
    void createPrivate_success() throws Exception {
      // given
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId));

      // when & then
      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.type").value(ChannelType.PRIVATE.name()));
    }

    @Test
    @DisplayName("fail with no valid participants")
    void createPrivate_fail_no_valid_participants_throws_exception() throws Exception {
      // given
      UUID nonExistentUserId = UUID.randomUUID();
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
          List.of(nonExistentUserId));

      // when & then
      ErrorCode errorCode = ErrorCode.NO_VALID_PARTICIPANTS;

      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.exceptionType")
              .value(NoValidParticipantsException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("success")
    void update_success() throws Exception {
      // given
      String newName = "updated";
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(newName, null);

      // when & then
      mockMvc.perform(patch("/api/channels/{channelId}", channelId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(channelId.toString()))
          .andExpect(jsonPath("$.name").value(newName));
    }

    @Test
    @DisplayName("fail with private channel update forbidden")
    void update_fail_private_channel_update_forbidden_throws_exception() throws Exception {
      // given
      ChannelResponse privateChannel = channelService.createPrivateChannel(
          new PrivateChannelCreateRequest(List.of(userId)));
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", null);

      // when & then
      ErrorCode errorCode = ErrorCode.PRIVATE_CHANNEL_UPDATE_FORBIDDEN;

      mockMvc.perform(patch("/api/channels/{channelId}", privateChannel.id())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.channelId").value(privateChannel.id().toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(PrivateChannelUpdateForbiddenException.class.getSimpleName()));
    }

    @Test
    @DisplayName("fail with channel not found")
    void update_fail_channel_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", null);

      // when & then
      ErrorCode errorCode = ErrorCode.CHANNEL_NOT_FOUND;

      mockMvc.perform(patch("/api/channels/{channelId}", nonExistentId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.channelId").value(nonExistentId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(ChannelNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("success")
    void delete_success() throws Exception {
      // when & then
      mockMvc.perform(delete("/api/channels/{channelId}", channelId)
              .with(csrf()))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("fail with channel not found")
    void delete_fail_channel_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();

      // when & then
      ErrorCode errorCode = ErrorCode.CHANNEL_NOT_FOUND;

      mockMvc.perform(delete("/api/channels/{channelId}", nonExistentId)
              .with(csrf()))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.channelId").value(nonExistentId.toString()))
          .andExpect(
              jsonPath("$.exceptionType").value(ChannelNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAllByUserId")
  class FindAllByUserId {

    @Test
    @DisplayName("success")
    void findAllByUserId_success() throws Exception {
      // given - create a private channel with userId as participant
      channelService.createPrivateChannel(new PrivateChannelCreateRequest(List.of(userId)));

      // when & then - user can see: 1 public channel (setUp) + 1 private channel = 2 total
      mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2));
    }
  }
}
