package com.sprint.mission.discodeit.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.response.PageResponse;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private MessageMapper messageMapper;

  @Mock
  private PageResponseMapper pageResponseMapper;

  @Mock
  private BinaryContentRepository binaryContentRepository; // 테스트코드에서 사용되지 않더라도 서비스코드에서 사용될 시 무조건 선언

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @InjectMocks
  private BasicMessageService messageService;

  @Test
  @DisplayName("메시지 생성 성공")
  void create_success() {
    //given
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest(userId, channelId, "안녕");

    User user = new User("이마크", "mark@test.com", "pass1234", null);
    Channel channel = new Channel("공지방", "공지방", ChannelType.PUBLIC);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(messageRepository.save(any(Message.class))).will(returnsFirstArg());

    UserDto userDto = new UserDto(userId, "이마크", "mark@test.com", null, null, null);
    MessageDto dto = new MessageDto(UUID.randomUUID(), null, null, userDto, channelId, "안녕", null);
    given(messageMapper.toDto(any(Message.class))).willReturn(dto);

    //when
    MessageDto result = messageService.create(request, List.of());

    //then
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo(request.content());
    then(messageRepository).should().save(any(Message.class));
  }

  @Test
  @DisplayName("존재하지 않는 채널에 생성 시 ChannelNotFoundException 발생")
  void create_fail_channel_not_found() {
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    MessageCreateRequest request = new MessageCreateRequest(userId, channelId, "안녕");
    User user = new User("이마크", "mark@test.com", "pass1234", null);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.create(request, null))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  @DisplayName("메시지 수정 성공")
  void update_success() {
    MessageUpdateRequest request = new MessageUpdateRequest("안녕하세요");

    User user = new User("이마크", "mark@test.com", "pass1234", null);
    Channel channel = new Channel("공지방", "공지방", ChannelType.PUBLIC);
    Message message = new Message(user, channel, "안녕", null);

    UserDto userDto = new UserDto(user.getId(), "이마크", "mark@test.com", null, null, null);
    MessageDto dto = new MessageDto(message.getId(), null, null, userDto, channel.getId(), "안녕하세요",
        null);

    given(messageRepository.findById(message.getId())).willReturn(Optional.of(message));
    given(messageMapper.toDto(any(Message.class))).willReturn(dto);

    MessageDto result = messageService.update(message.getId(), request);

    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo("안녕하세요");
    assertThat(message.getContent()).isEqualTo("안녕하세요");
  }

  @Test
  @DisplayName("존재하지 않는 메시지 수정 시 MessageNotFoundException 발생")
  void update_fail_message_not_found() {
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest request = new MessageUpdateRequest("안녕하세요");
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> messageService.update(messageId, request))
        .isInstanceOf(MessageNotFoundException.class);
  }

  @Test
  @DisplayName("특정 채널의 메시지 목록 조회 성공 - 메시지가 존재하는 경우")
  void findAllByChannelId_success_with_results() {
    Pageable pageable = PageRequest.of(0, 50);
    User user = new User("이마크", "mark@test.com", "pass1234", null);
    Channel channel = new Channel("공지방", "공지방", ChannelType.PUBLIC);
    Message message = new Message(user, channel, "안녕", null);

    // 구현체인 SliceImpl을 사용하여 가짜 Slice 객체 생성
    Slice<Message> messageSlice = new SliceImpl<>(List.of(message), pageable,
        false); // List.of(데이터리스트), 페이징정보, 다음페이지존재여부(hasNext)

    UserDto userDto = new UserDto(user.getId(), "이마크", "mark@test.com", null, null, null);
    MessageDto dto = new MessageDto(message.getId(), null, null, userDto, channel.getId(), "안녕하세요",
        null);
    PageResponse<MessageDto> dtoPageResponse = new PageResponse<>(List.of(dto), null, 50, false,
        null);

    given(channelRepository.existsById(channel.getId())).willReturn(true);
    given(messageRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId(),
        pageable)).willReturn(messageSlice);
    given(messageMapper.toDto(any(Message.class))).willReturn(dto);
    given(pageResponseMapper.fromSlice(any(Slice.class), any())).willReturn(
        dtoPageResponse); // 컴파일러가 헷갈려하던 <T, C> 제네릭 타입을 any(Slice.class)로 명시

    //when
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channel.getId(), null);

    //then
    assertThat(result).isNotNull();
    assertThat(result.content()).hasSize(1);
  }

  @Test
  @DisplayName("특정 채널의 메시지 목록 조회 성공 - 메시지가 없는 경우")
  void findAllByChannelId_success_empty_results() {
    UUID channelId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 50);

    Slice<Message> messageSlice = new SliceImpl<>(List.of(), pageable,
        false);

    PageResponse<MessageDto> dtoPageResponse = new PageResponse<>(List.of(), null, 50, false,
        null);

    given(channelRepository.existsById(channelId)).willReturn(true);
    given(messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId,
        pageable)).willReturn(messageSlice);
    given(pageResponseMapper.fromSlice(any(Slice.class), any())).willReturn(dtoPageResponse);

    //when
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null);

    assertThat(result).isNotNull();
    assertThat(result.content()).isEmpty();
  }
}
