package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.DuplicateReadStatusException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicReadStatusServiceTest {

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusMapper mapper;

  @InjectMocks
  private BasicReadStatusService readStatusService;

  private UUID readStatusId;
  private UUID userId;
  private UUID channelId;
  private User user;
  private Channel channel;
  private ReadStatus readStatus;
  private ReadStatusResponse response;

  @BeforeEach
  void setUp() {
    readStatusId = UUID.randomUUID();
    userId = UUID.randomUUID();
    channelId = UUID.randomUUID();
    user = new User("tester", "tester@example.io", "qwerty", null);
    ReflectionTestUtils.setField(user, "id", userId);
    channel = new Channel("general", "desc");
    ReflectionTestUtils.setField(channel, "id", channelId);
    readStatus = new ReadStatus(user, channel, Instant.now());
    ReflectionTestUtils.setField(readStatus, "id", readStatusId);
    response = new ReadStatusResponse(readStatusId, userId, channelId, Instant.now());
  }

  @Nested
  @DisplayName("create read status")
  class CreateReadStatus {

    @Test
    @DisplayName("success")
    void createReadStatus_success() {
      // given
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId,
          Instant.now());
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      given(readStatusRepository.existsByUserAndChannel(user, channel)).willReturn(false);
      given(mapper.toResponse(any(ReadStatus.class))).willReturn(response);

      // when
      ReadStatusResponse result = readStatusService.createReadStatus(request);

      // then
      assertThat(result).isEqualTo(response);
      then(readStatusRepository).should().save(any(ReadStatus.class));
    }

    @Test
    @DisplayName("fail with user not found")
    void createReadStatus_fail_user_not_found_throws_exception() {
      // given
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId,
          Instant.now());
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> readStatusService.createReadStatus(request))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("fail with channel not found")
    void createReadStatus_fail_channel_not_found_throws_exception() {
      // given
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId,
          Instant.now());
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> readStatusService.createReadStatus(request))
          .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("fail with duplicate read status")
    void createReadStatus_fail_duplicate_throws_exception() {
      // given
      ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId,
          Instant.now());
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      given(readStatusRepository.existsByUserAndChannel(user, channel)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> readStatusService.createReadStatus(request))
          .isInstanceOf(DuplicateReadStatusException.class);
    }
  }

  @Nested
  @DisplayName("find by id")
  class FindById {

    @Test
    @DisplayName("success")
    void findById_success() {
      // given
      given(readStatusRepository.findById(readStatusId)).willReturn(Optional.of(readStatus));
      given(mapper.toResponse(readStatus)).willReturn(response);

      // when
      ReadStatusResponse result = readStatusService.findById(readStatusId);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with read status not found")
    void findById_fail_not_found_throws_exception() {
      // given
      given(readStatusRepository.findById(readStatusId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> readStatusService.findById(readStatusId))
          .isInstanceOf(ReadStatusNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("find all by user id")
  class FindAllByUserId {

    @Test
    @DisplayName("success")
    void findAllByUserId_success() {
      // given
      given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of(readStatus));
      given(mapper.toResponse(readStatus)).willReturn(response);

      // when
      List<ReadStatusResponse> result = readStatusService.findAllByUserId(userId);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(response);
    }
  }

  @Nested
  @DisplayName("update read status")
  class UpdateReadStatus {

    @Test
    @DisplayName("success")
    void updateReadStatus_success() {
      // given
      Instant newLastReadAt = Instant.now();
      ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newLastReadAt);
      given(readStatusRepository.findById(readStatusId)).willReturn(Optional.of(readStatus));
      given(mapper.toResponse(readStatus)).willReturn(response);

      // when
      ReadStatusResponse result = readStatusService.updateReadStatus(readStatusId, request);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with read status not found")
    void updateReadStatus_fail_not_found_throws_exception() {
      // given
      ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(Instant.now());
      given(readStatusRepository.findById(readStatusId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> readStatusService.updateReadStatus(readStatusId, request))
          .isInstanceOf(ReadStatusNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("delete read status")
  class DeleteReadStatus {

    @Test
    @DisplayName("success")
    void deleteReadStatus_success() {
      // given
      given(readStatusRepository.findById(readStatusId)).willReturn(Optional.of(readStatus));

      // when
      readStatusService.deleteReadStatus(readStatusId);

      // then
      then(readStatusRepository).should().delete(readStatus);
    }

    @Test
    @DisplayName("fail with read status not found")
    void deleteReadStatus_fail_not_found_throws_exception() {
      // given
      given(readStatusRepository.findById(readStatusId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> readStatusService.deleteReadStatus(readStatusId))
          .isInstanceOf(ReadStatusNotFoundException.class);
    }
  }
}
