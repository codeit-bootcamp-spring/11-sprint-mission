package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private ReadStatusRepository readStatusRepository;
  @Mock
  private MessageRepository messageRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private ChannelMapper channelMapper;
  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private BasicChannelService channelService;

  // ───── createPublic ─────

  @Test
  void createPublic_성공() {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("general", "공용 채널");
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    ChannelDto dto = new ChannelDto(channel.getId(), "PUBLIC", "general", "공용 채널", List.of(), null);

    given(channelRepository.save(any(Channel.class))).willReturn(channel);
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(any())).willReturn(
        Optional.empty());
    given(channelMapper.toDto(any(Channel.class), any(), any())).willReturn(dto);

    // when
    ChannelDto result = channelService.createPublic(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.type()).isEqualTo("PUBLIC");
  }

  @Test
  void createPublic_실패_이름없음() {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(null, "설명");
    Channel channel = new Channel(ChannelType.PUBLIC, null, "설명");
    ChannelDto dto = new ChannelDto(channel.getId(), "PUBLIC", null, "설명", List.of(), null);

    given(channelRepository.save(any(Channel.class))).willReturn(channel);
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(any())).willReturn(
        Optional.empty());
    given(channelMapper.toDto(any(Channel.class), any(), any())).willReturn(dto);

    // when
    ChannelDto result = channelService.createPublic(request);

    // then
    assertThat(result.name()).isNull();
  }

  // ───── createPrivate ─────

  @Test
  void createPrivate_성공() {
    // given
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        List.of(userId1, userId2));
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    User user1 = new User("user1", "user1@test.com", "password", null);
    User user2 = new User("user2", "user2@test.com", "password", null);
    ChannelDto dto = new ChannelDto(channel.getId(), "PRIVATE", null, null, List.of(), null);

    given(channelRepository.save(any(Channel.class))).willReturn(channel);
    given(userRepository.findById(userId1)).willReturn(Optional.of(user1));
    given(userRepository.findById(userId2)).willReturn(Optional.of(user2));
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(any())).willReturn(
        Optional.empty());
    given(channelMapper.toDto(any(Channel.class), any(), any())).willReturn(dto);

    // when
    ChannelDto result = channelService.createPrivate(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.type()).isEqualTo("PRIVATE");
  }

  @Test
  void createPrivate_실패_존재하지않는유저() {
    // given
    UUID userId = UUID.randomUUID();
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId));
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);

    given(channelRepository.save(any(Channel.class))).willReturn(channel);
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> channelService.createPrivate(request))
        .isInstanceOf(Exception.class);
  }

  // ───── update ─────

  @Test
  void update_성공() {
    // given
    UUID channelId = UUID.randomUUID();
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "새 설명");
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    ChannelDto dto = new ChannelDto(channelId, "PUBLIC", "newName", "새 설명", List.of(), null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(any())).willReturn(
        Optional.empty());
    given(channelMapper.toDto(any(Channel.class), any(), any())).willReturn(dto);

    // when
    ChannelDto result = channelService.update(channelId, request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo("newName");
  }

  @Test
  void update_실패_존재하지않는채널() {
    // given
    UUID channelId = UUID.randomUUID();
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "새 설명");
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void update_실패_PRIVATE채널수정불가() {
    // given
    UUID channelId = UUID.randomUUID();
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "새 설명");
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    // when & then
    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(ChannelUpdateNotAllowedException.class);
  }

  // ───── delete ─────

  @Test
  void delete_성공() {
    // given
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    // when
    channelService.delete(channelId);

    // then
    then(channelRepository).should().delete(channel);
  }

  @Test
  void delete_실패_존재하지않는채널() {
    // given
    UUID channelId = UUID.randomUUID();
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> channelService.delete(channelId))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  // ───── findByUserId ─────

  @Test
  void findAllByUserId_성공() {
    // given
    UUID userId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    ChannelDto dto = new ChannelDto(channel.getId(), "PUBLIC", "general", "공용 채널", List.of(), null);

    given(channelRepository.findAllByType(ChannelType.PUBLIC)).willReturn(List.of(channel));
    given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(any())).willReturn(
        Optional.empty());
    given(channelMapper.toDto(any(Channel.class), any(), any())).willReturn(dto);

    // when
    var result = channelService.findAllByUserId(userId);

    // then
    assertThat(result).hasSize(1);
  }

  @Test
  void findAllByUserId_빈목록() {
    // given
    UUID userId = UUID.randomUUID();
    given(channelRepository.findAllByType(ChannelType.PUBLIC)).willReturn(List.of());
    given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());

    // when
    var result = channelService.findAllByUserId(userId);

    // then
    assertThat(result).isEmpty();
  }
}