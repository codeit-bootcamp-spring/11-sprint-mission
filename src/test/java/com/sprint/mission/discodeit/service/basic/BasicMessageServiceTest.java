package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageAttachment;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.mock.web.MockMultipartFile;
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
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private MessageMapper messageMapper;

  @InjectMocks
  private BasicMessageService messageService;

  private UUID messageId;
  private UUID channelId;
  private UUID authorId;
  private Channel mockChannel;
  private User mockAuthor;
  private Message mockMessage;
  private UserDto mockUserDto;
  private MessageDto mockMessageDto;

  @BeforeEach
  void setUp() {
    messageId = UUID.randomUUID();
    channelId = UUID.randomUUID();
    authorId = UUID.randomUUID();

    mockChannel = new Channel("test", "test", ChannelType.PUBLIC);
    ReflectionTestUtils.setField(mockChannel, "id", channelId);

    mockAuthor = new User("tester", "test@test.com", "password123", null);
    ReflectionTestUtils.setField(mockAuthor, "id", authorId);
    mockUserDto = new UserDto(authorId, "tester", "test@test.com", null, true, Role.USER);

    mockMessage = new Message(mockChannel, mockAuthor, "test message");
    mockMessageDto = new MessageDto(messageId, Instant.now(), Instant.now(), "test message",
        channelId, mockUserDto,
        Collections.emptyList());
  }

  @Nested
  @DisplayName("createMessage")
  class CreateMessage {

    // --- 성공 ---
    @Test
    @DisplayName("첨부파일 없이 메시지 생성")
    void createMessage_withoutAttachments_success() {
      // given
      CreateMessageRequest request = new CreateMessageRequest(
          channelId, authorId, "test message"
      );
      given(channelRepository.findById(channelId)).willReturn(Optional.of(mockChannel));
      given(userRepository.findById(authorId)).willReturn(Optional.of(mockAuthor));
      given(messageRepository.save(any(Message.class))).willReturn(mockMessage);
      given(messageMapper.toDto(any(Message.class))).willReturn(mockMessageDto);

      // when
      MessageDto result = messageService.createMessage(request, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.content()).isEqualTo("test message");
      then(messageRepository).should().save(any(Message.class));
      then(binaryContentRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("첨부파일과 함께 메시지 생성")
    void createMessage_withAttachments_success() {
      // given
      CreateMessageRequest request = new CreateMessageRequest(
          channelId, authorId, "test message"
      );
      MockMultipartFile file = new MockMultipartFile(
          "file", "test.png", "image/png", "fake-bytes".getBytes()
      );
      BinaryContent savedBinaryContent = new BinaryContent("test.png", "image/png", 10L);
      UUID binaryId = UUID.randomUUID();
      ReflectionTestUtils.setField(savedBinaryContent, "id", binaryId);

      given(channelRepository.findById(channelId)).willReturn(Optional.of(mockChannel));
      given(userRepository.findById(authorId)).willReturn(Optional.of(mockAuthor));
      given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(savedBinaryContent);
      given(messageRepository.save(any(Message.class))).willReturn(mockMessage);
      given(messageMapper.toDto(any(Message.class))).willReturn(mockMessageDto);

      // when
      MessageDto result = messageService.createMessage(request, List.of(file));

      // then
      assertThat(result).isNotNull();
      then(binaryContentRepository).should().save(any(BinaryContent.class));
      then(eventPublisher).should().publishEvent(any(BinaryContentCreatedEvent.class));
    }

    // --- 실패 ---
    @Test
    @DisplayName("존재하지 않는 channelId일 경우 ChannelNotFoundException이 발생")
    void createMessage_channelNotFound_throwsChannelNotFoundException() {
      // given
      CreateMessageRequest request = new CreateMessageRequest(
          channelId, authorId, "test message"
      );
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.createMessage(request, null))
          .isInstanceOf(ChannelNotFoundException.class);
      then(messageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 authorId일 경우 UserNotFoundException이 발생")
    void createMessage_userNotFound_throwsUserNotFoundException() {
      // given
      CreateMessageRequest request = new CreateMessageRequest(
          channelId, authorId, "test message"
      );
      given(channelRepository.findById(channelId)).willReturn(Optional.of(mockChannel));
      given(userRepository.findById(authorId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.createMessage(request, null))
          .isInstanceOf(UserNotFoundException.class);
      then(messageRepository).should(never()).save(any());
    }
  }

  @Nested
  @DisplayName("readMessageByChannel")
  class ReadMessageByChannel {

    @Test
    @DisplayName("cursor가 null일 경우 최근 메시지부터 size + 1개를 조회")
    void readMessagesByChannel_noCursor_success() {
      // given
      given(messageRepository.findByChannelIdOrderByCreatedAtDesc(eq(channelId), any(Limit.class)))
          .willReturn(List.of(mockMessage));
      given(messageMapper.toDto(mockMessage)).willReturn(mockMessageDto);

      // when
      PageResponse<MessageDto> result = messageService.readMessagesByChannel(channelId, null, 10);

      // then
      assertThat(result.content()).hasSize(1);
      assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("corsor가 있을 경우 해당 시점 이전 메시지를 조회")
    void readMessageByChannel_withCursor_success() {
      // given
      Instant cursor = Instant.now();

      given(messageRepository.findByChannelIdAndCreatedAtBeforeOrderByCreatedAtDesc(
          eq(channelId), eq(cursor), any(Limit.class)
      )).willReturn(List.of(mockMessage));
      given(messageMapper.toDto(mockMessage)).willReturn(mockMessageDto);

      // when
      PageResponse<MessageDto> result = messageService.readMessagesByChannel(
          channelId, cursor, 10
      );

      // then
      assertThat(result.content()).hasSize(1);
      assertThat(result.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("deleteMessage")
  class DeleteMessage {

    // --- 성공 ---
    @Test
    @DisplayName("첨부파일이 없는 메시지가 정상적으로 삭제됨")
    void deleteMessage_withoutAttachments_success() {
      // given
      given(messageRepository.findById(messageId)).willReturn(Optional.of(mockMessage));

      // when
      messageService.deleteMessage(messageId);

      // then
      then(binaryContentRepository).should(never()).deleteAll(any());
      then(messageRepository).should().delete(mockMessage);
    }

    @Test
    @DisplayName("첨뷰파일이 있는 메시지 삭제 시 BinaryContent도 함께 삭제됨")
    void deleteMessage_withAttachments_success() {
      // given
      BinaryContent binary = new BinaryContent("file.png", "image/png", 10L);
      MessageAttachment attachment = new MessageAttachment(mockMessage, binary);
      ReflectionTestUtils.setField(mockMessage, "messageAttachments", List.of(attachment));

      given(messageRepository.findById(messageId)).willReturn(Optional.of(mockMessage));

      // when
      messageService.deleteMessage(messageId);

      // then
      then(binaryContentRepository).should().deleteAll(List.of(binary));
      then(messageRepository).should().delete(mockMessage);
    }

    // --- 실패 ---
    @Test
    @DisplayName("존재하지 않는 messageId일 경우 MessageNotFoundException이 발생")
    void deleteMessage_messageNotFound_throwsMessageNotFoundException() {
      // given
      given(messageRepository.findById(messageId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.deleteMessage(messageId))
          .isInstanceOf(MessageNotFoundException.class);
      then(messageRepository).should(never()).delete(any());
    }
  }

  @Nested
  @DisplayName("updateMessage")
  class UpdateMessage {

    // --- 성공 ---
    @Test
    @DisplayName("메시지가 정상적으로 업데이트됨")
    void updateMessage_success() {
      // given
      MessageUpdateRequest request = new MessageUpdateRequest("new message");
      MessageDto updatedDto = new MessageDto(messageId, Instant.now(), Instant.now(), "new message",
          channelId, mockUserDto,
          Collections.emptyList());
      given(messageRepository.findById(messageId)).willReturn(Optional.of(mockMessage));
      given(messageMapper.toDto(any(Message.class))).willReturn(updatedDto);

      // when
      MessageDto result = messageService.updateMessage(messageId, request);

      // then
      assertThat(result.content()).isEqualTo("new message");
    }

    // --- 실패 ---
    @Test
    @DisplayName("존재하지 하지 않는 messageId일 경우 MessageNotFoundException이 발생")
    void updateMessage_messageNotFound_throwsMessageNotFoundException() {
      // given
      given(messageRepository.findById(messageId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(
          () -> messageService.updateMessage(messageId, new MessageUpdateRequest("new message")))
          .isInstanceOf(MessageNotFoundException.class);
    }
  }
}