package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @InjectMocks
  private BasicMessageService messageService;

  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private MessageMapper messageMapper;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private PageResponseMapper pageResponseMapper;

  @Test
  void create_succeeds() {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    Channel channel = new Channel(ChannelType.PUBLIC, "general", null);
    User author = new User("testuser", "test@example.com", "pass", null);
    Message message = new Message("안녕하세요", channel, author, List.of());
    MessageDto expectedDto = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), "안녕하세요", channelId,
        new UserDto(authorId, "testuser", "test@example.com", null, false), List.of());

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(messageRepository.save(any(Message.class))).willReturn(message);
    given(messageMapper.toDto(any(Message.class))).willReturn(expectedDto);

    MessageDto result = messageService.create(request, List.of());

    assertThat(result).isEqualTo(expectedDto);
    then(messageRepository).should().save(any(Message.class));
  }

  @Test
  void create_withAttachments_succeeds() {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("파일 첨부", channelId, authorId);
    BinaryContentCreateRequest attachmentRequest = new BinaryContentCreateRequest("file.txt", "text/plain", new byte[]{1, 2, 3});
    Channel channel = new Channel(ChannelType.PUBLIC, "general", null);
    User author = new User("testuser", "test@example.com", "pass", null);
    BinaryContent binaryContent = new BinaryContent("file.txt", 3L, "text/plain");
    Message message = new Message("파일 첨부", channel, author, List.of(binaryContent));
    MessageDto expectedDto = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), "파일 첨부", channelId,
        new UserDto(authorId, "testuser", "test@example.com", null, false), List.of());

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(binaryContent);
    given(messageRepository.save(any(Message.class))).willReturn(message);
    given(messageMapper.toDto(any(Message.class))).willReturn(expectedDto);

    MessageDto result = messageService.create(request, List.of(attachmentRequest));

    assertThat(result).isEqualTo(expectedDto);
    then(binaryContentRepository).should().save(any(BinaryContent.class));
    then(binaryContentStorage).should().put(any(), any(byte[].class));
  }

  @Test
  void create_withNonExistentChannel_throwsException() {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);

    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.create(request, List.of()))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void create_withNonExistentUser_throwsException() {
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    Channel channel = new Channel(ChannelType.PUBLIC, "general", null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.create(request, List.of()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void find_succeeds() {
    UUID messageId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "general", null);
    User author = new User("testuser", "test@example.com", "pass", null);
    Message message = new Message("내용", channel, author, List.of());
    ReflectionTestUtils.setField(message, "id", messageId);
    UUID channelId = UUID.randomUUID();
    MessageDto expectedDto = new MessageDto(messageId, Instant.now(), Instant.now(), "내용", channelId,
        new UserDto(UUID.randomUUID(), "testuser", "test@example.com", null, false), List.of());

    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
    given(messageMapper.toDto(message)).willReturn(expectedDto);

    MessageDto result = messageService.find(messageId);

    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void find_withNonExistentMessage_throwsException() {
    UUID messageId = UUID.randomUUID();

    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.find(messageId))
        .isInstanceOf(MessageNotFoundException.class);
  }

  @Test
  void findAllByChannelId_succeeds() {
    UUID channelId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 20);
    Instant cursor = Instant.now();

    UUID msgId = UUID.randomUUID();
    Instant msgCreatedAt = Instant.now().minusSeconds(10);
    MessageDto messageDto = new MessageDto(msgId, msgCreatedAt, msgCreatedAt, "내용", channelId,
        new UserDto(UUID.randomUUID(), "user", "user@example.com", null, false), List.of());
    Slice<MessageDto> slice = new SliceImpl<>(List.of(messageDto), pageable, false);
    PageResponse<MessageDto> expectedPage = new PageResponse<>(List.of(messageDto), msgCreatedAt, 20, false, null);

    given(messageRepository.findAllByChannelIdWithAuthor(any(UUID.class), any(Instant.class), any(Pageable.class)))
        .willReturn(new SliceImpl<>(List.of(), pageable, false));
    given(pageResponseMapper.fromSlice(any(), any())).willAnswer(inv -> expectedPage);

    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, cursor, pageable);

    assertThat(result.content()).hasSize(1);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void update_succeeds() {
    UUID messageId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "general", null);
    User author = new User("testuser", "test@example.com", "pass", null);
    Message message = new Message("원래 내용", channel, author, List.of());
    ReflectionTestUtils.setField(message, "id", messageId);
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    UUID channelId = UUID.randomUUID();
    MessageDto expectedDto = new MessageDto(messageId, Instant.now(), Instant.now(), "수정된 내용", channelId,
        new UserDto(UUID.randomUUID(), "testuser", "test@example.com", null, false), List.of());

    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
    given(messageMapper.toDto(message)).willReturn(expectedDto);

    MessageDto result = messageService.update(messageId, request);

    assertThat(result.content()).isEqualTo("수정된 내용");
  }

  @Test
  void update_withNonExistentMessage_throwsException() {
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");

    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.update(messageId, request))
        .isInstanceOf(MessageNotFoundException.class);
  }

  @Test
  void delete_succeeds() {
    UUID messageId = UUID.randomUUID();

    given(messageRepository.existsById(messageId)).willReturn(true);

    messageService.delete(messageId);

    then(messageRepository).should().deleteById(messageId);
  }

  @Test
  void delete_withNonExistentMessage_throwsException() {
    UUID messageId = UUID.randomUUID();

    given(messageRepository.existsById(messageId)).willReturn(false);

    assertThatThrownBy(() -> messageService.delete(messageId))
        .isInstanceOf(MessageNotFoundException.class);
  }
}