package com.sprint.mission.discodeit.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private MessageMapper messageMapper;

  @Mock
  private PageResponseMapper pageResponseMapper;

  @InjectMocks
  private BasicMessageService messageService;

  @Test
  @DisplayName("메시지 생성 성공(첨부파일 없음)")
  void create_success_no_attachments() {
    // given
    UUID channelId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Channel channel = mock(Channel.class);
    User user = mock(User.class);

    MessageCreateRequest request = new MessageCreateRequest("메시지", channelId, userId);
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(messageRepository.save(any(Message.class))).willAnswer(i -> i.getArgument(0));

    // when
    Message result = messageService.create(request, null);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo("메시지");
    then(messageRepository).should().save(any(Message.class));
    then(binaryContentRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("메시지 생성 성공(첨부파일 있음)")
  void create_success_with_attachments() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Channel channel = mock(Channel.class);
    User user = mock(User.class);

    MessageCreateRequest request = new MessageCreateRequest("메시지", channelId, userId);

    MultipartFile file = mock(MultipartFile.class);

    given(file.getOriginalFilename()).willReturn("test.png");
    given(file.getSize()).willReturn(100L);
    given(file.getContentType()).willReturn("image/png");
    given(file.getBytes()).willReturn("data".getBytes());

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(messageRepository.save(any(Message.class))).willAnswer(i -> i.getArgument(0));

    given(binaryContentStorage.put(any(), any())).willReturn(null);

    given(binaryContentRepository.save(any())).willAnswer(i -> i.getArgument(0));

    // when
    Message result = messageService.create(request, List.of(file));

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo("메시지");
    assertThat(result.getAttachments()).hasSize(1);

    then(messageRepository).should().save(any(Message.class));
    then(binaryContentRepository).should().save(any());
    then(binaryContentStorage).should().put(any(), any());
  }

  @Test
  @DisplayName("메시지 생성 실패(채널이 존재하지 않음)")
  void create_fail_notfound_channel() {
    // given
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest("메시지", channelId, userId);

    // 채널 없음
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> messageService.create(request, null)).isInstanceOf(DiscodeitException.class);

    then(messageRepository).should(never()).save(any(Message.class));

  }

  @Test
  @DisplayName("메시지 생성 실패(유저가 존재하지 않음)")
  void create_fail_notfound_user() {
    // given
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest("메시지", channelId, userId);

    // 채널은 있지만, 유저 없음
    given(channelRepository.findById(channelId)).willReturn(Optional.of(mock(Channel.class)));
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> messageService.create(request, null)).isInstanceOf(DiscodeitException.class);

    then(messageRepository).should(never()).save(any(Message.class));

  }

  @Test
  @DisplayName("메시지 수정 성공")
  void update_success() {
    // given
//    Message message = mock(Message.class);
    Message message = Message.create("메시지", mock(Channel.class), mock(User.class));

    MessageUpdateRequest request = new MessageUpdateRequest("수정 메시지");
    given(messageRepository.findById(message.getId())).willReturn(Optional.of(message));

    // when
    Message result = messageService.update(message.getId(), request);

    // then
    assertThat(result.getContent()).isEqualTo("수정 메시지");
    then(messageRepository).should().save(any(Message.class));

  }

  @Test
  @DisplayName("메시지 수정 실패(메시지가 존재하지 않음)")
  void update_fail() {
    // given
    Message message = Message.create("메시지", mock(Channel.class), mock(User.class));

    MessageUpdateRequest request = new MessageUpdateRequest("수정 메시지");

    // 메시지ID로 메시지를 조회 했지만 없음
    given(messageRepository.findById(message.getId())).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () ->
            messageService.update(message.getId(), request)).isInstanceOf(DiscodeitException.class);

    then(messageRepository).should(never()).save(any(Message.class));

  }

  @Test
  @DisplayName("메시지 삭제 성공(첨부파일 없음)")
  void delete_success() {
    // given
    Message message = mock(Message.class);
    given(messageRepository.findById(message.getId())).willReturn(Optional.of(message));

    // when
    messageService.delete(message.getId());

    // then
    then(messageRepository).should().deleteById(message.getId());
  }

  @Test
  @DisplayName("메시지 삭제 성공(첨부파일 있음)")
  void delete_success_withAttachments() {
    // given
    UUID messageId = UUID.randomUUID();

    Message message = mock(Message.class);
    BinaryContent attachment = mock(BinaryContent.class);
    List<BinaryContent> attachments = List.of(attachment);

    given(message.getId()).willReturn(messageId);
    given(message.getAttachments()).willReturn(attachments);
    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

    // when
    messageService.delete(messageId);

    // then
    then(binaryContentRepository).should().deleteAll(attachments);
    then(messageRepository).should().deleteById(messageId);
  }

  @Test
  @DisplayName("메시지 삭제 실패(메시지가 존재하지 않음)")
  void delete_fail() {
    // given
    Message message = mock(Message.class);

    // 메시지ID로 메시지를 조회했지만 없음
    given(messageRepository.findById(message.getId())).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () ->
            messageService.delete(message.getId())).isInstanceOf(DiscodeitException.class);

    then(messageRepository).should(never()).deleteById(message.getId());
  }

  @Test
  @DisplayName("채널ID로 메시지 조회 성공")
  void findByChannelId_success() {
    // given
    UUID channelId = UUID.randomUUID();
    Instant cursor = Instant.now();

    Message message1 = mock(Message.class);
    Message message2 = mock(Message.class);

    List<Message> messages = List.of(message1, message2);

    Slice<Message> slice = new SliceImpl<>(messages, PageRequest.of(0, 50), true);

    MessageDto dto1 = mock(MessageDto.class);
    MessageDto dto2 = mock(MessageDto.class);

    PageResponse<MessageDto> response =
        new PageResponse<>(List.of(dto1, dto2), null, 50, true, null);

    given(messageRepository.findMessages(eq(channelId), eq(cursor), any(Pageable.class)))
        .willReturn(slice);

    given(messageMapper.toDto(message1)).willReturn(dto1);
    given(messageMapper.toDto(message2)).willReturn(dto2);
    given(pageResponseMapper.fromSlice(any(), isNull())).willReturn(response);

    // when
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, cursor);

    // then
    assertThat(result).isNotNull();
    assertThat(result.content()).hasSize(2);
    assertThat(result.nextCursor()).isEqualTo(message2.getCreatedAt());

    then(messageRepository).should()
        .findMessages(eq(channelId), eq(cursor), any(Pageable.class));
  }

  @Test
  @DisplayName("채널ID로 메시지 조회 성공(메시지가 없음)")
  void findByChannelId_success_emptyMessages() {
    // given
    UUID channelId = UUID.randomUUID();
    Instant cursor = Instant.now();

    Slice<Message> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 50), false);

    PageResponse<MessageDto> response =
        new PageResponse<>(List.of(), null, 50, false, null);

    given(messageRepository.findMessages(eq(channelId), eq(cursor), any(Pageable.class)))
        .willReturn(emptySlice);
    given(pageResponseMapper.fromSlice(any(), isNull())).willReturn(response);

    // when
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, cursor);

    // then
    assertThat(result).isNotNull();
    assertThat(result.content()).isEmpty();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.hasNext()).isFalse();

    then(messageRepository).should()
        .findMessages(eq(channelId), eq(cursor), any(Pageable.class));
    then(messageMapper).should(never()).toDto(any());
    then(pageResponseMapper).should().fromSlice(any(), isNull());
  }

}
