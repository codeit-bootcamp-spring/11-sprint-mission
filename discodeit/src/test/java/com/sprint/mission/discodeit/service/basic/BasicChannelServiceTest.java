package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.DuplicateChannelException;
import com.sprint.mission.discodeit.exception.channel.NoValidParticipantsException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateForbiddenException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
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
class BasicChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private ChannelMapper mapper;

  @InjectMocks
  private BasicChannelService channelService;

  private UUID channelId;
  private UUID userId;
  private String name;
  private String description;
  private Channel publicChannel;
  private Channel privateChannel;
  private User user;
  private ChannelResponse response;

  @BeforeEach
  void setUp() {
    channelId = UUID.randomUUID();
    userId = UUID.randomUUID();
    name = "testName";
    description = "testDescription";
    publicChannel = new Channel(name, description);
    ReflectionTestUtils.setField(publicChannel, "id", channelId);
    privateChannel = new Channel();
    ReflectionTestUtils.setField(privateChannel, "id", channelId);
    user = new User("tester", "tester@example.io", "qwerty", null);
    ReflectionTestUtils.setField(user, "id", userId);
    response = new ChannelResponse(channelId, ChannelType.PUBLIC, name, description, List.of(),
        Instant.now());
  }

  @Nested
  @DisplayName("create public channel")
  class CreatePublicChannel {

    @Test
    @DisplayName("success")
    void createPublicChannel_success() {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest(name, description);
      given(channelRepository.existsByName(name)).willReturn(false);
      given(mapper.toResponse(any(Channel.class), anyList(), nullable(Instant.class)))
          .willReturn(response);

      // when
      ChannelResponse result = channelService.createPublicChannel(request);

      // then
      assertThat(result).isEqualTo(response);
      then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("fail with duplicate channel name")
    void createPublicChannel_fail_duplicate_name_throws_exception() {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest(name, description);
      given(channelRepository.existsByName(name)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> channelService.createPublicChannel(request))
          .isInstanceOf(DuplicateChannelException.class);
    }
  }

  @Nested
  @DisplayName("create private channel")
  class CreatePrivateChannel {

    @Test
    @DisplayName("success")
    void createPrivateChannel_success() {
      // given
      List<UUID> participantIds = List.of(userId);
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);
      given(userRepository.findAllById(participantIds)).willReturn(List.of(user));
      given(mapper.toResponse(any(Channel.class), anyList(), nullable(Instant.class)))
          .willReturn(response);

      // when
      ChannelResponse result = channelService.createPrivateChannel(request);

      // then
      assertThat(result).isEqualTo(response);
      then(channelRepository).should().save(any(Channel.class));
      then(readStatusRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("fail with no valid participants")
    void createPrivateChannel_fail_no_valid_participants_throws_exception() {
      // given
      List<UUID> participantIds = List.of(UUID.randomUUID());
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);
      given(userRepository.findAllById(anyList())).willReturn(List.of());

      // when & then
      assertThatThrownBy(() -> channelService.createPrivateChannel(request))
          .isInstanceOf(NoValidParticipantsException.class);
    }
  }

  @Nested
  @DisplayName("find by id")
  class FindById {

    @Test
    @DisplayName("success")
    void findById_success() {
      // given
      ReadStatus readStatus = new ReadStatus(user, publicChannel, Instant.now());
      given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
      given(readStatusRepository.findAllByChannel(publicChannel)).willReturn(List.of(readStatus));
      given(messageRepository.findTopCreatedAtByChannelOrderByCreatedAtDesc(publicChannel))
          .willReturn(Optional.of(Instant.now()));
      given(mapper.toResponse(any(Channel.class), anyList(), nullable(Instant.class)))
          .willReturn(response);

      // when
      ChannelResponse result = channelService.findById(channelId);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with channel not found")
    void findById_fail_channel_not_found_throws_exception() {
      // given
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.findById(channelId))
          .isInstanceOf(ChannelNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("find all by user id")
  class FindAllByUserId {

    @Test
    @DisplayName("success with results")
    void findAllByUserId_success() {
      // given
      given(channelRepository.findAllByUserId(userId)).willReturn(List.of(publicChannel));
      given(readStatusRepository.findAllByChannelIn(List.of(publicChannel))).willReturn(List.of());
      given(messageRepository.findLastMessageAtByChannelIds(anyList())).willReturn(List.of());
      given(mapper.toResponse(any(Channel.class), anyList(), nullable(Instant.class)))
          .willReturn(response);

      // when
      List<ChannelResponse> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(response);
    }

    @Test
    @DisplayName("success with empty list")
    void findAllByUserId_success_empty() {
      // given
      given(channelRepository.findAllByUserId(userId)).willReturn(List.of());
      given(readStatusRepository.findAllByChannelIn(List.of())).willReturn(List.of());
      given(messageRepository.findLastMessageAtByChannelIds(List.of())).willReturn(List.of());

      // when
      List<ChannelResponse> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("update channel")
  class UpdateChannel {

    @Test
    @DisplayName("success")
    void updateChannel_success() {
      // given
      String newName = "newName";
      String newDescription = "newDescription";
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(newName, newDescription);
      given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
      given(channelRepository.existsByName(newName)).willReturn(false);
      given(readStatusRepository.findAllByChannel(publicChannel)).willReturn(List.of());
      given(messageRepository.findTopCreatedAtByChannelOrderByCreatedAtDesc(publicChannel))
          .willReturn(Optional.empty());
      given(mapper.toResponse(any(Channel.class), anyList(), nullable(Instant.class)))
          .willReturn(response);

      // when
      ChannelResponse result = channelService.updateChannel(channelId, request);

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with channel not found")
    void updateChannel_fail_channel_not_found_throws_exception() {
      // given
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "newDesc");
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.updateChannel(channelId, request))
          .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("fail when updating private channel")
    void updateChannel_fail_private_channel_throws_exception() {
      // given
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "newDesc");
      given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

      // when & then
      assertThatThrownBy(() -> channelService.updateChannel(channelId, request))
          .isInstanceOf(PrivateChannelUpdateForbiddenException.class);
    }

    @Test
    @DisplayName("fail with duplicate channel name")
    void updateChannel_fail_duplicate_name_throws_exception() {
      // given
      String newName = "duplicateName";
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest(newName, "newDesc");
      given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
      given(channelRepository.existsByName(newName)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> channelService.updateChannel(channelId, request))
          .isInstanceOf(DuplicateChannelException.class);
    }
  }

  @Nested
  @DisplayName("delete channel")
  class DeleteChannel {

    @Test
    @DisplayName("success")
    void deleteChannel_success() {
      // given
      given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));

      // when
      channelService.deleteChannel(channelId);

      // then
      then(messageRepository).should().deleteAllByChannel(publicChannel);
      then(readStatusRepository).should().deleteAllByChannel(publicChannel);
      then(channelRepository).should().delete(publicChannel);
    }

    @Test
    @DisplayName("fail with channel not found")
    void deleteChannel_fail_channel_not_found_throws_exception() {
      // given
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.deleteChannel(channelId))
          .isInstanceOf(ChannelNotFoundException.class);
    }
  }
}