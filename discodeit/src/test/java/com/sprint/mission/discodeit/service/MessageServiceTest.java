package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.service.dto.message.MessageDto;
import com.sprint.mission.discodeit.service.dto.message.UpdateMessageRequest;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private MessageMapper messageMapper;
    @Mock private PageResponseMapper pageResponseMapper;
    @Mock private BinaryContentStorage binaryContentStorage;

    @InjectMocks private MessageService messageService;

    private User user;
    private Channel channel;
    private Message message;
    private MessageDto messageDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();
        channel = Channel.publicChannel("일반", "일반 채널");
        message = new Message(user, channel, "테스트 메시지");
        messageDto = MessageDto.builder()
                .id(message.getId())
                .content("테스트 메시지")
                .channelId(channel.getId())
                .attachments(List.of())
                .build();
    }

    @Test
    void create_성공() {
        CreateMessageRequest request = new CreateMessageRequest(
                user.getId(), channel.getId(), "테스트 메시지", List.of()
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
        given(messageRepository.save(any(Message.class))).willReturn(message);
        given(messageMapper.toDto(message)).willReturn(messageDto);

        MessageDto result = messageService.create(request);

        assertThat(result).isEqualTo(messageDto);
        then(messageRepository).should().save(any(Message.class));
    }

    @Test
    void create_작성자_없음_예외() {
        UUID unknownUserId = UUID.randomUUID();
        CreateMessageRequest request = new CreateMessageRequest(
                unknownUserId, channel.getId(), "내용", List.of()
        );

        given(userRepository.findById(unknownUserId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void create_채널_없음_예외() {
        UUID unknownChannelId = UUID.randomUUID();
        CreateMessageRequest request = new CreateMessageRequest(
                user.getId(), unknownChannelId, "내용", List.of()
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(unknownChannelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    void update_성공() {
        UUID messageId = message.getId();
        UpdateMessageRequest request = new UpdateMessageRequest(messageId, "수정된 메시지");

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(messageMapper.toDto(message)).willReturn(messageDto);

        MessageDto result = messageService.update(request);

        assertThat(result).isEqualTo(messageDto);
    }

    @Test
    void update_메시지_없음_예외() {
        UUID messageId = UUID.randomUUID();
        UpdateMessageRequest request = new UpdateMessageRequest(messageId, "수정 내용");

        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.update(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void delete_성공() {
        UUID messageId = message.getId();
        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        messageService.delete(messageId);

        then(messageRepository).should().delete(message);
    }

    @Test
    void delete_메시지_없음_예외() {
        UUID messageId = UUID.randomUUID();
        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.delete(messageId))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void findAllByChannelId_성공() {
        UUID channelId = channel.getId();
        SliceImpl<Message> messageSlice = new SliceImpl<>(
                List.of(message), PageRequest.of(0, 50), false
        );
        PageResponse<MessageDto> expectedPage = PageResponse.<MessageDto>builder()
                .content(List.of(messageDto))
                .size(50)
                .hasNext(false)
                .build();

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(messageRepository.findAllByChannelIdOrderByCreatedAtDesc(any(), any()))
                .willReturn(messageSlice);
        given(messageMapper.toDto(message)).willReturn(messageDto);
        given(pageResponseMapper.fromSlice(any(), any())).willAnswer(inv -> expectedPage);

        PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null, 50);

        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.content()).hasSize(1);
    }

    @Test
    void findAllByChannelId_채널_없음_예외() {
        UUID unknownChannelId = UUID.randomUUID();
        given(channelRepository.findById(unknownChannelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.findAllByChannelId(unknownChannelId, null, 50))
                .isInstanceOf(ChannelNotFoundException.class);
    }
}
