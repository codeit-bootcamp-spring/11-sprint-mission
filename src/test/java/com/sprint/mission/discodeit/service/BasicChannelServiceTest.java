package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
  ChannelRepository channelRepository;
  @Mock
  ReadStatusRepository readStatusRepository;
  @Mock
  MessageRepository messageRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  ChannelMapper channelMapper;

  @InjectMocks
  BasicChannelService channelService;

  // find()
  @Test
  void createPrivate_존재하지않는참여자_예외발생하고저장하지않음() {
    UUID participantId = UUID.randomUUID();
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(participantId));
    given(userRepository.findAllById(List.of(participantId))).willReturn(List.of());

    assertThatThrownBy(() -> channelService.create(request))
        .isInstanceOf(UserNotFoundException.class);

    then(channelRepository).should(never()).save(any(Channel.class));
    then(readStatusRepository).shouldHaveNoInteractions();
    then(channelMapper).shouldHaveNoInteractions();
  }

  // find()
  @Test
  void find_존재하지않는채널_예외발생() {
    UUID channelId = UUID.randomUUID();
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> channelService.find(channelId))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  // update()
  @Test
  void update_존재하지않는채널_예외발생() {
    UUID channelId = UUID.randomUUID();
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "newDesc");
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void update_프라이빗채널수정_예외발생() {
    UUID channelId = UUID.randomUUID();
    Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "newDesc");
    given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(PrivateChannelUpdateException.class);
  }

  @Test
  void update_정상_채널수정() {
    UUID channelId = UUID.randomUUID();
    Channel publicChannel = new Channel(ChannelType.PUBLIC, "oldName", "oldDesc");
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("newName", "newDesc");
    ChannelDto channelDto = new ChannelDto(channelId, ChannelType.PUBLIC, "newName", "newDesc", List.of(), null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
    given(channelMapper.toDto(any(Channel.class))).willReturn(channelDto);

    channelService.update(channelId, request);

    verify(channelMapper).toDto(publicChannel);
  }

  // delete()
  @Test
  void delete_존재하지않는채널_예외발생() {
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(false);

    assertThatThrownBy(() -> channelService.delete(channelId))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void delete_정상_삭제호출() {
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(true);

    channelService.delete(channelId);

    verify(channelRepository).deleteById(channelId);
    verify(messageRepository).deleteAllByChannelId(channelId);
    verify(readStatusRepository).deleteAllByChannelId(channelId);
  }
}
