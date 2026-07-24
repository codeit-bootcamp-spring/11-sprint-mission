package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
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
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private MessageMapper messageMapper;
  @Mock
  private PageResponseMapper pageResponseMapper;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private BasicMessageService messageService;

  // Create 테스트
  @Test
  @DisplayName("메시지 생성 성공")
  void create_success() {
    // Given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    Channel mockChannel = Channel.createPublic("공개 채널", "공개 채널입니다.");
    User mockUser = User.builder().username("woody").build();

    MessageDto.CreateRequest request = new MessageDto.CreateRequest("안녕하세요!", channelId, authorId);

    MessageDto.Response mockResponse = MessageDto.Response.builder()
        .content(request.content())
        .build();

    given(channelRepository.findById(request.channelId())).willReturn(Optional.of(mockChannel));
    given(userRepository.findById(request.authorId())).willReturn(Optional.of(mockUser));
    given(messageMapper.toDto(any(Message.class))).willReturn(mockResponse);

    // When
    MessageDto.Response result = messageService.create(request, null);

    // Then
    assertThat(result.content()).isEqualTo(request.content());

    then(messageRepository).should().save(any(Message.class));
  }

  @Test
  @DisplayName("존재하지 않는 채널에 메시지 생성 시 ChannelNotFoundException 발생")
  void create_fail_channelNotFound() {
    // Given
    UUID notExistingChannelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageDto.CreateRequest request = new MessageDto.CreateRequest("안녕하세요!", notExistingChannelId,
        authorId);

    given(channelRepository.findById(request.channelId())).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> messageService.create(request, null))
        .isInstanceOf(ChannelNotFoundException.class);

    then(userRepository).shouldHaveNoInteractions();
    then(messageRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("존재하지 않는 작성자로 메시지 생성 시 UserNotFoundException 발생")
  void create_fail_authorNotFound() {
    // Given
    UUID channelId = UUID.randomUUID();
    UUID notExistingAuthorId = UUID.randomUUID();
    Channel mockChannel = Channel.createPublic("공개 채널", "설명");
    MessageDto.CreateRequest request = new MessageDto.CreateRequest("안녕하세요!", channelId,
        notExistingAuthorId);

    given(channelRepository.findById(request.channelId())).willReturn(Optional.of(mockChannel));
    given(userRepository.findById(request.authorId())).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> messageService.create(request, null))
        .isInstanceOf(UserNotFoundException.class);

    then(channelRepository).should().findById(request.channelId());
    then(userRepository).should().findById(request.authorId());
    then(messageRepository).shouldHaveNoInteractions();
  }

  // Update 테스트
  @Test
  @DisplayName("메시지 수정 성공")
  void update_success() {
    // Given
    UUID messageId = UUID.randomUUID();
    Channel mockChannel = Channel.createPublic("채널", "설명");
    User mockUser = User.builder().username("woody").build();

    Message existingMessage = Message.builder()
        .channel(mockChannel)
        .author(mockUser)
        .content("old content")
        .build();

    MessageDto.UpdateRequest request = new MessageDto.UpdateRequest("new content");

    MessageDto.Response mockResponse = MessageDto.Response.builder()
        .content(request.newContent())
        .build();

    given(messageRepository.findById(messageId)).willReturn(Optional.of(existingMessage));
    given(messageMapper.toDto(any(Message.class))).willReturn(mockResponse);

    // When
    MessageDto.Response result = messageService.update(messageId, request);

    // Then
    assertThat(result.content()).isEqualTo(request.newContent());
    assertThat(existingMessage.getContent()).isEqualTo(request.newContent()); // 더티 체킹 검증
  }

  @Test
  @DisplayName("존재하지 않는 메시지 수정 시 MessageNotFoundException 발생")
  void update_fail_messageNotFound() {
    // Given
    UUID notExistingId = UUID.randomUUID();
    MessageDto.UpdateRequest request = new MessageDto.UpdateRequest("new content");

    given(messageRepository.findById(notExistingId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> messageService.update(notExistingId, request))
        .isInstanceOf(MessageNotFoundException.class);
  }

  // Delete 테스트
  @Test
  @DisplayName("메시지 삭제 성공")
  void delete_success() {
    // Given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.existsById(messageId)).willReturn(true);

    // When
    messageService.delete(messageId);

    // Then
    then(messageRepository).should().deleteById(messageId);
  }

  @Test
  @DisplayName("존재하지 않는 메시지 삭제 시 MessageNotFoundException 발생")
  void delete_fail_messageNotFound() {
    // Given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.existsById(messageId)).willReturn(false);

    // When & Then
    assertThatThrownBy(() -> messageService.delete(messageId))
        .isInstanceOf(MessageNotFoundException.class);

    then(messageRepository).shouldHaveNoMoreInteractions();
  }

  // 4. findAllByChannelId 테스트
  @Test
  @DisplayName("채널별 메시지 목록 조회 - 첫 페이지")
  void findAllByChannelId_firstPage_success() {
    // Given
    UUID channelId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 2);

    Channel mockChannel = Channel.createPublic("채널", "설명");
    User mockUser = User.builder().username("woody").build();

    Instant time1 = Instant.now().minusSeconds(10);
    Instant time2 = Instant.now().minusSeconds(5);
    Message message1 = Message.builder().channel(mockChannel).author(mockUser).content("메시지1")
        .build();
    Message message2 = Message.builder().channel(mockChannel).author(mockUser).content("메시지2")
        .build();
    ReflectionTestUtils.setField(message1, "createdAt", time1);
    ReflectionTestUtils.setField(message2, "createdAt", time2);

    MessageDto.Response response1 = MessageDto.Response.builder().content("메시지1").build();
    MessageDto.Response response2 = MessageDto.Response.builder().content("메시지2").build();

    Slice<Message> slice = new SliceImpl<>(List.of(message1, message2), pageable, true);

    PageResponse<MessageDto.Response> mockPageResponse = new PageResponse<>(
        List.of(response1, response2),
        time2,
        2,
        true,
        2L
    );

    given(messageRepository.findAllByChannelId(eq(channelId), any(), eq(pageable))).willReturn(
        slice);
    given(messageMapper.toDto(message1)).willReturn(response1);
    given(messageMapper.toDto(message2)).willReturn(response2);
    given(pageResponseMapper.<MessageDto.Response>fromSlice(any(), eq(time2))).willReturn(
        mockPageResponse);

    // When
    PageResponse<MessageDto.Response> result = messageService.findAllByChannelId(channelId, null,
        pageable);

    // Then
    assertThat(result).isEqualTo(mockPageResponse);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo(time2);
  }

  @Test
  @DisplayName("채널별 메시지 목록 조회 - 마지막 페이지")
  void findAllByChannelId_lastPage_success() {
    // Given
    UUID channelId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(1, 2);
    Instant cursor = Instant.now();

    Channel channel = Channel.createPublic("채널", "설명");
    User author = User.builder().username("woody").build();

    Message message3 = Message.builder().channel(channel).author(author).content("메시지3").build();
    MessageDto.Response response3 = MessageDto.Response.builder().content("메시지3").build();

    Slice<Message> slice = new SliceImpl<>(List.of(message3), pageable, false);

    PageResponse<MessageDto.Response> mockPageResponse = new PageResponse<>(
        List.of(response3),
        null,
        2,
        false,
        1L
    );

    given(messageRepository.findAllByChannelId(eq(channelId), any(), eq(pageable))).willReturn(
        slice);
    given(messageMapper.toDto(message3)).willReturn(response3);
    given(pageResponseMapper.<MessageDto.Response>fromSlice(any(), any())).willReturn(
        mockPageResponse);

    // When
    PageResponse<MessageDto.Response> result = messageService.findAllByChannelId(channelId, cursor,
        pageable);

    // Then
    assertThat(result).isEqualTo(mockPageResponse);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }

  @Test
  @DisplayName("채널별 메시지 목록 조회 - 결과 없음")
  void findAllByChannelId_empty() {
    // Given
    UUID channelId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);
    Instant cursor = Instant.now();

    Slice<Message> emptySlice = new SliceImpl<>(Collections.emptyList(), pageable, false);

    PageResponse<MessageDto.Response> mockPageResponse = new PageResponse<>(
        Collections.emptyList(),
        null,
        10,
        false,
        0L
    );

    given(messageRepository.findAllByChannelId(eq(channelId), any(), eq(pageable)))
        .willReturn(emptySlice);

    given(pageResponseMapper.<MessageDto.Response>fromSlice(any(), any()))
        .willReturn(mockPageResponse);

    // When
    PageResponse<MessageDto.Response> result = messageService.findAllByChannelId(channelId, cursor,
        pageable);

    // Then
    assertThat(result).isEqualTo(mockPageResponse);
    assertThat(result.content()).isEmpty();
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }
}