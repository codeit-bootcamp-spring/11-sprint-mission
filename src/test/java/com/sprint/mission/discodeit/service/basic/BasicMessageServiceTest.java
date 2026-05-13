package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
public class BasicMessageServiceTest {

  @InjectMocks
  private BasicMessageService messageService;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private MessageMapper messageMapper;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Mock
  private PageResponseMapper pageResponseMapper;

  @Test
  @DisplayName("메시지 생성 성공")
  void create_success() {
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요"
        , authorId, channelId);

    User author = new User("username", "email", "password");
    Channel channel = new Channel(ChannelType.PUBLIC, "채널", "설명");
    MessageDto dto = new MessageDto(UUID.randomUUID(), Instant.now(),
        Instant.now(), "안녕하세요", channelId, null, List.of());

    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(messageMapper.toDto(any(Message.class))).willReturn(dto);

    MessageDto result = messageService.create(request, null);

    assertThat(result.content()).isEqualTo("안녕하세요");
    then(messageRepository).should().save(any(Message.class));
  }

  @Test
  @DisplayName("메시지 생성 실패 - 존재하지 않는 채널")
  void create_fail_channelNotFound() {
    UUID authorId = UUID.randomUUID();
    UUID invalidChannelId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest(
        "내용", authorId, invalidChannelId);

    given(userRepository.findById(authorId)).willReturn(Optional.of(
        new User("유저", "mail", "pw")));
    given(channelRepository.findById(invalidChannelId)).willReturn(Optional.empty());

    assertThrows(ChannelNotFoundException.class, () -> {
      messageService.create(request, null);
    });
  }

  @Test
  @DisplayName("채널별 메시지 목록 조회")
  void findAllByChannelId_success() {
    UUID channelId = UUID.randomUUID();
    PageRequest pageRequest = PageRequest.of(0, 50);

    Slice<Message> slice = new SliceImpl<>(Collections.emptyList());

    given(messageRepository.findByChannelIdOrderByCreatedAtDesc(eq(channelId), eq(pageRequest)))
        .willReturn(slice);

    List<MessageDto> emptyList = java.util.Collections.emptyList();

    PageResponse<MessageDto> response = new PageResponse<>(emptyList,
        null, 50, false, 0L);

    given(pageResponseMapper.fromSlice(any(), any())).willReturn((PageResponse) response);
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null, 50);

    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("메시지 수정 성공")
  void update_success() {
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
    Message message = new Message("원래 내용", null, null);
    MessageDto dto = new MessageDto(messageId, Instant.now(), Instant.now(),
        "수정된 내용", UUID.randomUUID(), null, List.of());

    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
    given(messageMapper.toDto(message)).willReturn(dto);

    MessageDto result = messageService.update(messageId, request);

    assertThat(result.content()).isEqualTo("수정된 내용");
  }

  @Test
  @DisplayName("메시지 수정 실패 - 메시지 없음")
  void update_fail_notFound() {
    UUID invalidId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("내용");

    given(messageRepository.findById(invalidId)).willReturn(Optional.empty());

    assertThrows(MessageNotFoundException.class, () -> {
      messageService.update(invalidId, request);
    });
  }

  @Test
  @DisplayName("메시지 삭제 성공")
  void delete_success() {
    UUID messageId = UUID.randomUUID();

    messageService.delete(messageId);

    then(messageRepository).should().deleteById(messageId);
  }
}
