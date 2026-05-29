package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.common.PageResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageWithoutChannelAccessException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private MessageMapper mapper;

  @Mock
  private PageMapper pageMapper;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @InjectMocks
  private BasicMessageService messageService;

  private UUID messageId;
  private UUID channelId;
  private UUID authorId;
  private Channel publicChannel;
  private Channel privateChannel;
  private User author;
  private Message message;
  private MessageResponse response;

  @BeforeEach
  void setUp() {
    messageId = UUID.randomUUID();
    channelId = UUID.randomUUID();
    authorId = UUID.randomUUID();
    publicChannel = new Channel("general", "desc");
    ReflectionTestUtils.setField(publicChannel, "id", channelId);
    privateChannel = new Channel();
    ReflectionTestUtils.setField(privateChannel, "id", channelId);
    author = new User("tester", "tester@example.io", "qwerty", null);
    ReflectionTestUtils.setField(author, "id", authorId);
    message = new Message("hello", publicChannel, author, List.of());
    ReflectionTestUtils.setField(message, "id", messageId);
    response = new MessageResponse(messageId, Instant.now(), Instant.now(), "hello", channelId,
        null, List.of());
  }

  @Nested
  @DisplayName("create message")
  class CreateMessage {

    @Test
    @DisplayName("success")
    void createMessage_success() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);
      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
      given(mapper.toResponse(any(Message.class))).willReturn(response);

      // when
      MessageResponse result = messageService.createMessage(request, List.of());

      // then
      assertThat(result).isEqualTo(response);
      then(messageRepository).should().save(any(Message.class));
    }

    @Test
    @DisplayName("success - private channel with access")
    void createMessage_success_private_channel_with_access() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);
      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));
      given(readStatusRepository.existsByUserAndChannel(author, privateChannel)).willReturn(true);
      given(mapper.toResponse(any(Message.class))).willReturn(response);

      // when
      MessageResponse result = messageService.createMessage(request, List.of());

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with user not found")
    void createMessage_fail_user_not_found_throws_exception() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);
      given(userRepository.findById(authorId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.createMessage(request, List.of()))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("fail with channel not found")
    void createMessage_fail_channel_not_found_throws_exception() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);
      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.createMessage(request, List.of()))
          .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("fail with private channel access denied")
    void createMessage_fail_private_channel_access_denied_throws_exception() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("hello", channelId, authorId);
      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));
      given(readStatusRepository.existsByUserAndChannel(author, privateChannel)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> messageService.createMessage(request, List.of()))
          .isInstanceOf(MessageWithoutChannelAccessException.class);
    }
  }

  @Nested
  @DisplayName("find all by channel id")
  class FindAllByChannelId {

    @Test
    @DisplayName("success without cursor")
    void findAllByChannelId_success_without_cursor() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      Slice<Message> slice = new SliceImpl<>(List.of(message), pageable, false);
      PageResponse<MessageResponse> pageResponse = new PageResponse<>(List.of(response), null, 10,
          false, null);
      given(channelRepository.existsById(channelId)).willReturn(true);
      given(messageRepository.findAllByChannelIdOrderByCreatedAtDesc(channelId, pageable))
          .willReturn(slice);
      given(mapper.toResponse(message)).willReturn(response);
      given(pageMapper.<MessageResponse>fromSlice(any(), any())).willReturn(pageResponse);

      // when
      PageResponse<MessageResponse> result = messageService.findAllByChannelId(channelId, null,
          pageable);

      // then
      assertThat(result).isEqualTo(pageResponse);
    }

    @Test
    @DisplayName("success with cursor")
    void findAllByChannelId_success_with_cursor() {
      // given
      Instant cursor = Instant.now();
      Pageable pageable = PageRequest.of(0, 10);
      Slice<Message> slice = new SliceImpl<>(List.of(message), pageable, false);
      PageResponse<MessageResponse> pageResponse = new PageResponse<>(List.of(response), null, 10,
          false, null);
      given(channelRepository.existsById(channelId)).willReturn(true);
      given(messageRepository.findAllByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(channelId,
          cursor, pageable)).willReturn(slice);
      given(mapper.toResponse(message)).willReturn(response);
      given(pageMapper.<MessageResponse>fromSlice(any(), any())).willReturn(pageResponse);

      // when
      PageResponse<MessageResponse> result = messageService.findAllByChannelId(channelId, cursor,
          pageable);

      // then
      assertThat(result).isEqualTo(pageResponse);
    }

    @Test
    @DisplayName("fail with channel not found")
    void findAllByChannelId_fail_channel_not_found_throws_exception() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      given(channelRepository.existsById(channelId)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> messageService.findAllByChannelId(channelId, null, pageable))
          .isInstanceOf(ChannelNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("update message")
  class UpdateMessage {

    @Test
    @DisplayName("success")
    void updateMessage_success() {
      // given
      MessageUpdateRequest request = new MessageUpdateRequest("new content");
      given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
      given(mapper.toResponse(message)).willReturn(response);

      // when
      MessageResponse result = messageService.updateMessage(messageId, request, List.of());

      // then
      assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("fail with message not found")
    void updateMessage_fail_message_not_found_throws_exception() {
      // given
      MessageUpdateRequest request = new MessageUpdateRequest("new content");
      given(messageRepository.findById(messageId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.updateMessage(messageId, request, List.of()))
          .isInstanceOf(MessageNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("delete message")
  class DeleteMessage {

    @Test
    @DisplayName("success")
    void deleteMessage_success() {
      // given
      given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

      // when
      messageService.deleteMessage(messageId);

      // then
      then(messageRepository).should().delete(message);
    }

    @Test
    @DisplayName("fail with message not found")
    void deleteMessage_fail_message_not_found_throws_exception() {
      // given
      given(messageRepository.findById(messageId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.deleteMessage(messageId))
          .isInstanceOf(MessageNotFoundException.class);
    }
  }
}
