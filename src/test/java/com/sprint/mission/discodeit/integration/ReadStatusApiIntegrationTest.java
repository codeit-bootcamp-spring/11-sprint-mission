package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.readstatus.DuplicateReadStatusException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
class ReadStatusApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private ChannelService channelService;

  @Autowired
  private ReadStatusService readStatusService;

  private UUID userId;
  private UUID channelId;
  private UUID readStatusId;

  @BeforeEach
  void setUp() {
    UserResponse user = userService.createUser(
        new UserCreateRequest("tester", "tester@example.io", "password1234"),
        Optional.empty()
    );
    userId = user.id();

    ChannelResponse channel = channelService.createPublicChannel(
        new PublicChannelCreateRequest("general", "description")
    );
    channelId = channel.id();

    ReadStatusResponse readStatus = readStatusService.createReadStatus(
        new ReadStatusCreateRequest(userId, channelId, Instant.now())
    );
    readStatusId = readStatus.id();
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("success")
    void create_success() throws Exception {
      // given - create a second channel to attach a new read status
      ChannelResponse anotherChannel = channelService.createPublicChannel(
          new PublicChannelCreateRequest("another-channel", null)
      );
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(
          userId, anotherChannel.id(), Instant.now());

      // when & then
      mockMvc.perform(post("/api/read-statuses")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.userId").value(userId.toString()))
          .andExpect(jsonPath("$.channelId").value(anotherChannel.id().toString()));
    }

    @Test
    @DisplayName("fail with duplicate read status")
    void create_fail_duplicate_read_status_throws_exception() throws Exception {
      // given - read status for userId + channelId already exists from setUp
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(
          userId, channelId, Instant.now());

      // when & then
      ErrorCode errorCode = ErrorCode.DUPLICATE_READ_STATUS;

      mockMvc.perform(post("/api/read-statuses")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.userId").value(userId.toString()))
          .andExpect(jsonPath("$.details.channelId").value(channelId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(DuplicateReadStatusException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("success")
    void update_success() throws Exception {
      // given
      ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(Instant.now());

      // when & then
      mockMvc.perform(patch("/api/read-statuses/{readStatusId}", readStatusId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(readStatusId.toString()))
          .andExpect(jsonPath("$.userId").value(userId.toString()))
          .andExpect(jsonPath("$.channelId").value(channelId.toString()));
    }

    @Test
    @DisplayName("fail with read status not found")
    void update_fail_read_status_not_found_throws_exception() throws Exception {
      // given
      UUID nonExistentId = UUID.randomUUID();
      ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(Instant.now());

      // when & then
      ErrorCode errorCode = ErrorCode.READ_STATUS_NOT_FOUND;

      mockMvc.perform(patch("/api/read-statuses/{readStatusId}", nonExistentId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.details.readStatusId").value(nonExistentId.toString()))
          .andExpect(jsonPath("$.exceptionType")
              .value(ReadStatusNotFoundException.class.getSimpleName()));
    }
  }

  @Nested
  @DisplayName("findAllByUserId")
  class FindAllByUserId {

    @Test
    @DisplayName("success")
    void findAllByUserId_success() throws Exception {
      // when & then
      mockMvc.perform(get("/api/read-statuses").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].id").value(readStatusId.toString()))
          .andExpect(jsonPath("$[0].userId").value(userId.toString()))
          .andExpect(jsonPath("$[0].channelId").value(channelId.toString()));
    }
  }
}