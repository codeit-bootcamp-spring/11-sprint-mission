package com.sprint.mission.discodeit.sevice;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.messagedto.MessageDto;
import com.sprint.mission.discodeit.dto.messagedto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.messagedto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.channel.NonExistChannelException;
import com.sprint.mission.discodeit.exception.service.message.NonExistMessageException;
import com.sprint.mission.discodeit.exception.service.user.NonExistUserException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.ArrayList;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

  @Mock
  JPAMessageRepository messageRepository;
  @Mock
  JPAChannelRepository channelRepository;
  @Mock
  JPAUserRepository userRepository;

  @Mock
  MessageMapper messageMapper;
  @Mock
  PageResponseMapper pageResponseMapper;
  @Mock
  BinaryContentStorage binaryContentStorage;

  @InjectMocks
  BasicMessageService messageService;


  @Test
  @DisplayName("메시지 생성 성공 테스트")
  void createMessageTest() {

    //given
    MessageCreateRequest request = new MessageCreateRequest(
        "메시지 내용",
        UUID.randomUUID(),
        UUID.randomUUID()
    );

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(new User()));
    given(channelRepository.findById(any(UUID.class))).willReturn(Optional.of(new Channel()));

    given(messageRepository.saveAndFlush(any(Message.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    given(messageMapper.toDto(any(Message.class))).willReturn(new MessageDto(

        UUID.randomUUID(),
        Instant.now(),
        Instant.now(),
        request.content(),
        request.channelId(),
        null,
        new ArrayList<>()
    ));
    //when
    MessageDto dto = messageService.create(request, null);

    //then

    assertThat(dto).isNotNull();
    then(messageRepository).should().saveAndFlush(any(Message.class));

  }

  @Test
  @DisplayName("없는 작성자")
  void createMessageFailByNonExistAuthor() {

    //given
    MessageCreateRequest request = new MessageCreateRequest(
        "메시지 내용",
        UUID.randomUUID(),
        UUID.randomUUID()
    );

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> messageService.create(request, null)).isInstanceOf(
        NonExistUserException.class);


  }

  @Test
  @DisplayName("없는 채널")
  void createMessageFailByNonExistChannel() {

    //given
    MessageCreateRequest request = new MessageCreateRequest(
        "메시지 내용",
        UUID.randomUUID(),
        UUID.randomUUID()
    );

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(new User()));
    given(channelRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> messageService.create(request, null)).isInstanceOf(
        NonExistChannelException.class);


  }

  @Test
  @DisplayName("메시지 수정")
  void updateMessageTest() {

    UUID messageId = UUID.randomUUID();

    //given

    MessageUpdateRequest request = new MessageUpdateRequest(
        "새 메시지"

    );

    UUID channelId = UUID.randomUUID();

    given(messageRepository.findById(messageId)).willReturn(Optional.of(new Message()));

    given(messageMapper.toDto(any(Message.class))).willReturn(new MessageDto(

        messageId,
        Instant.now(),
        Instant.now(),
        request.newContent(),
        channelId,
        null,
        new ArrayList<>()
    ));

    //when
    MessageDto result = messageService.update(messageId, request);

    //then
    assertThat(result).isNotNull();
    then(messageRepository).should(times(1)).findById(messageId);

  }


  @Test
  @DisplayName("메시지 수정")
  void updateFailMessageTest() {

    UUID messageId = UUID.randomUUID();

    //given

    MessageUpdateRequest request = new MessageUpdateRequest(
        "새 메시지"

    );

    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    //when&then
    assertThatThrownBy(() -> messageService.update(messageId, request)).isInstanceOf(
        NonExistMessageException.class);


  }


  @Test
  @DisplayName("채널 내 메시지 출력")
  void findAllMessageByChannelIdTest() {
    //given

    Message message1 = new Message(
        null,
        null,
        "content1",
        null

    );
    Message message2 = new Message(
        null,
        null,
        "content2",
        null

    );

    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(true);

    List<Message> list = new ArrayList<>();
    list.add(message1);
    list.add(message2);

    Pageable pageable = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));
    Slice<Message> slice = new SliceImpl<>(list, pageable, false);

    given(messageRepository.findAllByChannel_Id(channelId, pageable)).willReturn(slice);

    MessageDto dto1 = new MessageDto(
        message1.getId(),
        Instant.now(),
        Instant.now(),
        message1.getContent(),
        channelId,
        null,
        new ArrayList<>()
    );
    MessageDto dto2 = new MessageDto(
        message2.getId(),
        Instant.now(),
        Instant.now(),
        message2.getContent(),
        channelId,
        null,
        new ArrayList<>()
    );

    given(messageMapper.toDto(any(Message.class))).willReturn(dto1, dto2);

    List<MessageDto> dtoList = List.of(dto1, dto2);

    Slice<MessageDto> sliceDto = new SliceImpl<>(dtoList, slice.getPageable(), slice.hasNext());

    PageResponse<MessageDto> fakeResponse = new PageResponse<>(dtoList, null, 10, false, 2L);
    given(pageResponseMapper.fromSlice(sliceDto, null)).willReturn(fakeResponse);

    //when
    PageResponse<MessageDto> response = messageService.findAllByChannelId(channelId, pageable,
        null);

    //then
    assertThat(response).isNotNull();
    then(messageMapper).should(times(2)).toDto(any(Message.class));
    then(pageResponseMapper).should(times(1)).fromSlice(any(Slice.class), isNull());
    assertThat(response.getNextCursor()).isNull();


  }

  @Test
  @DisplayName("유효 채널 아이디 아닐때의 채널 전체 메시지 요청")
  void findAllMessageByChannelIdFailTest() {

    UUID channelId = UUID.randomUUID();

    //given

    given(channelRepository.existsById(channelId)).willReturn(false);

    //when&then
    assertThatThrownBy(() -> messageService.findAllByChannelId(channelId, null, null)).isInstanceOf(
        NonExistChannelException.class);


  }


}
