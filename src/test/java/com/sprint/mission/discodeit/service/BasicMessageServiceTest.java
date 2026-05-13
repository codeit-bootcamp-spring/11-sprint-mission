package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

  @Mock
  MessageRepository messageRepository;
  @Mock
  ChannelRepository channelRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  MessageMapper messageMapper;
  @Mock
  BinaryContentStorage binaryContentStorage;
  @Mock
  BinaryContentRepository binaryContentRepository;
  @Mock
  PageResponseMapper pageResponseMapper;

  @InjectMocks
  BasicMessageService messageService;

  // create()
  @Test
  void create_존재하지않는채널_예외발생() {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);

    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.create(request, List.of()))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void create_존재하지않는유저_예외발생() {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);
    Channel channel = new Channel(ChannelType.PUBLIC, "test", "desc");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.create(request, List.of()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void create_정상_메시지저장() {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);
    Channel channel = new Channel(ChannelType.PUBLIC, "test", "desc");
    User author = new User("testuser", "test@test.com", "password", null);
    MessageDto messageDto = new MessageDto(UUID.randomUUID(), null, null, "hello", channelId, null, List.of());

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(messageMapper.toDto(any(Message.class))).willReturn(messageDto);

    messageService.create(request, List.of());

    verify(messageRepository).save(any(Message.class));
  }

  // find()
  @Test
  void find_존재하지않는메시지_예외발생() {
    UUID messageId = UUID.randomUUID();
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.find(messageId))
        .isInstanceOf(MessageNotFoundException.class);
  }

  // update()
  @Test
  void update_존재하지않는메시지_예외발생() {
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("newContent");
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.update(messageId, request))
        .isInstanceOf(MessageNotFoundException.class);
  }

  // delete()
  @Test
  void delete_존재하지않는메시지_예외발생() {
    UUID messageId = UUID.randomUUID();
    given(messageRepository.existsById(messageId)).willReturn(false);

    assertThatThrownBy(() -> messageService.delete(messageId))
        .isInstanceOf(MessageNotFoundException.class);
  }

  @Test
  void delete_정상_삭제호출() {
    UUID messageId = UUID.randomUUID();
    given(messageRepository.existsById(messageId)).willReturn(true);

    messageService.delete(messageId);

    verify(messageRepository).deleteById(messageId);
  }
}