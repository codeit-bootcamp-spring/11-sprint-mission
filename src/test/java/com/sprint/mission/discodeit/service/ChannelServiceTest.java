package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

  @InjectMocks
  private BasicChannelService channelService;

  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private ReadStatusRepository readStatusRepository;
  @Mock
  private MessageRepository messageRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ChannelMapper channelMapper;

  @Test
  void createPublic_succeeds() {
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("general", "일반 채널");
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "일반 채널");
    ChannelDto expectedDto = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "general", "일반 채널", List.of(), null);

    given(channelRepository.save(any(Channel.class))).willReturn(channel);
    given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

    ChannelDto result = channelService.create(request);

    assertThat(result).isEqualTo(expectedDto);
    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  void createPrivate_succeeds() {
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId1, userId2));
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    User user1 = new User("user1", "user1@example.com", "pass1", null);
    User user2 = new User("user2", "user2@example.com", "pass2", null);
    ChannelDto expectedDto = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, null, null, List.of(), null);

    given(channelRepository.save(any(Channel.class))).willReturn(channel);
    given(userRepository.findAllById(List.of(userId1, userId2))).willReturn(List.of(user1, user2));
    given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

    ChannelDto result = channelService.create(request);

    assertThat(result).isEqualTo(expectedDto);
    then(readStatusRepository).should().saveAll(anyList());
  }

  @Test
  void find_succeeds() {
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "일반 채널");
    ReflectionTestUtils.setField(channel, "id", channelId);
    ChannelDto expectedDto = new ChannelDto(channelId, ChannelType.PUBLIC, "general", "일반 채널", List.of(), null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(channelMapper.toDto(channel)).willReturn(expectedDto);

    ChannelDto result = channelService.find(channelId);

    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void find_withNonExistentChannel_throwsException() {
    UUID channelId = UUID.randomUUID();

    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> channelService.find(channelId))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void findAllByUserId_succeeds() {
    UUID userId = UUID.randomUUID();
    UUID privateChannelId = UUID.randomUUID();

    Channel publicChannel = new Channel(ChannelType.PUBLIC, "general", null);
    Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
    ReflectionTestUtils.setField(privateChannel, "id", privateChannelId);

    User user = new User("testuser", "test@example.com", "pass", null);
    ReadStatus readStatus = new ReadStatus(user, privateChannel, Instant.now());

    ChannelDto publicDto = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "general", null, List.of(), null);
    ChannelDto privateDto = new ChannelDto(privateChannelId, ChannelType.PRIVATE, null, null, List.of(), null);

    given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of(readStatus));
    given(channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), anyList()))
        .willReturn(List.of(publicChannel, privateChannel));
    given(channelMapper.toDto(publicChannel)).willReturn(publicDto);
    given(channelMapper.toDto(privateChannel)).willReturn(privateDto);

    List<ChannelDto> result = channelService.findAllByUserId(userId);

    assertThat(result).hasSize(2).containsExactlyInAnyOrder(publicDto, privateDto);
  }

  @Test
  void update_succeeds() {
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "old", "old desc");
    ReflectionTestUtils.setField(channel, "id", channelId);
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("new", "new desc");
    ChannelDto expectedDto = new ChannelDto(channelId, ChannelType.PUBLIC, "new", "new desc", List.of(), null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(channelMapper.toDto(channel)).willReturn(expectedDto);

    ChannelDto result = channelService.update(channelId, request);

    assertThat(result.name()).isEqualTo("new");
    assertThat(result.description()).isEqualTo("new desc");
  }

  @Test
  void update_withNonExistentChannel_throwsException() {
    UUID channelId = UUID.randomUUID();
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("new", "new desc");

    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void update_onPrivateChannel_throwsException() {
    UUID channelId = UUID.randomUUID();
    Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
    ReflectionTestUtils.setField(privateChannel, "id", channelId);
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("new", "new desc");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(PrivateChannelUpdateException.class);
  }

  @Test
  void delete_succeeds() {
    UUID channelId = UUID.randomUUID();

    given(channelRepository.existsById(channelId)).willReturn(true);

    channelService.delete(channelId);

    then(messageRepository).should().deleteAllByChannelId(channelId);
    then(readStatusRepository).should().deleteAllByChannelId(channelId);
    then(channelRepository).should().deleteById(channelId);
  }

  @Test
  void delete_withNonExistentChannel_throwsException() {
    UUID channelId = UUID.randomUUID();

    given(channelRepository.existsById(channelId)).willReturn(false);

    assertThatThrownBy(() -> channelService.delete(channelId))
        .isInstanceOf(ChannelNotFoundException.class);
  }
}