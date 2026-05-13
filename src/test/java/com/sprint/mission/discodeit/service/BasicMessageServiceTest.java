package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private MessageMapper messageMapper;
  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private BasicMessageService messageService;

  // ───── create ─────

  @Test
  void create_성공() {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);

    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    User author = new User("jihye", "jihye@test.com", "password123", null);
    Message message = new Message("안녕하세요", channel, author);
    UserStatus status = new UserStatus(author, Instant.now());
    MessageDto dto = new MessageDto(message.getId(), Instant.now(), Instant.now(), "안녕하세요",
        channelId, null, List.of());

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(messageRepository.saveAndFlush(any(Message.class))).willReturn(message);
    given(userStatusRepository.findByUserId(any())).willReturn(Optional.of(status));
    given(userMapper.toDto(any(User.class), any(UserStatus.class))).willReturn(null);
    given(messageMapper.toDto(any(Message.class), any(), any())).willReturn(dto);

    // when
    MessageDto result = messageService.create(request, null);

    // then
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo("안녕하세요");
  }

  @Test
  void create_실패_존재하지않는채널() {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> messageService.create(request, null))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  void create_실패_존재하지않는유저() {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> messageService.create(request, null))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ───── update ─────

  @Test
  void update_성공() {
    // given
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    User author = new User("jihye", "jihye@test.com", "password123", null);
    Message message = new Message("안녕하세요", channel, author);
    UserStatus status = new UserStatus(author, Instant.now());
    MessageDto dto = new MessageDto(messageId, Instant.now(), Instant.now(), "수정된 내용",
        channel.getId(), null, List.of());

    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
    given(userStatusRepository.findByUserId(any())).willReturn(Optional.of(status));
    given(userMapper.toDto(any(User.class), any(UserStatus.class))).willReturn(null);
    given(messageMapper.toDto(any(Message.class), any(), any())).willReturn(dto);

    // when
    MessageDto result = messageService.update(messageId, request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo("수정된 내용");
  }

  @Test
  void update_실패_존재하지않는메시지() {
    // given
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> messageService.update(messageId, request))
        .isInstanceOf(MessageNotFoundException.class);
  }

  // ───── delete ─────

  @Test
  void delete_성공() {
    // given
    UUID messageId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    User author = new User("jihye", "jihye@test.com", "password123", null);
    Message message = new Message("안녕하세요", channel, author);
    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

    // when
    messageService.delete(messageId);

    // then
    then(messageRepository).should().delete(message);
  }

  @Test
  void delete_실패_존재하지않는메시지() {
    // given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> messageService.delete(messageId))
        .isInstanceOf(MessageNotFoundException.class);
  }

  // ───── findByChannelId ─────

  @Test
  void findAllByChannelId_성공() {
    // given
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "general", "공용 채널");
    User author = new User("jihye", "jihye@test.com", "password123", null);
    Message message = new Message("안녕하세요", channel, author);
    MessageDto dto = new MessageDto(message.getId(), Instant.now(), Instant.now(), "안녕하세요",
        channelId, null, List.of());

    given(messageRepository.findAllByChannelIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
        .willReturn(new SliceImpl<>(List.of(message)));
    given(userStatusRepository.findAllByUserIdIn(any())).willReturn(List.of());
    given(messageMapper.toDto(any(Message.class), any(), any())).willReturn(dto);

    // when
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null, 50);

    // then
    assertThat(result.content()).hasSize(1);
  }

  @Test
  void findAllByChannelId_빈목록() {
    // given
    UUID channelId = UUID.randomUUID();
    given(messageRepository.findAllByChannelIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
        .willReturn(new SliceImpl<>(List.of()));
    given(userStatusRepository.findAllByUserIdIn(any())).willReturn(List.of());

    // when
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null, 50);

    // then
    assertThat(result.content()).isEmpty();
  }
}