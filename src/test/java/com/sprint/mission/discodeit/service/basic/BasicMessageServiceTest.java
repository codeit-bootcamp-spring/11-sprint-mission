package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateParam;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.security.UserOnlineStatusResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

    @Mock
    MessageRepository messageRepository;

    @Mock
    ChannelRepository channelRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ReadStatusRepository readStatusRepository;

    @Mock
    BinaryContentRepository binaryContentRepository;

    @Mock
    MessageMapper messageMapper;

    @Mock
    BinaryContentStorage binaryContentStorage;

    @Mock
    UserOnlineStatusResolver userOnlineStatusResolver;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    BasicMessageService messageService;

    @Test
    void create_success() {
        // given
        User author = new User("evan", "evan@test.com", "password");
        User receiver = new User("wendy", "wendy@test.com", "password");
        Channel channel = new Channel("general", "general channel");
        ReadStatus readStatus = new ReadStatus(receiver, channel, Instant.now());

        MessageCreateRequest request = new MessageCreateRequest(
                author.getId(),
                channel.getId(),
                "hello",
                null
        );

        Message savedMessage = new Message(author, channel, request.content());

        MessageDto expectedDto = new MessageDto(
                savedMessage.getId(),
                savedMessage.getCreatedAt(),
                savedMessage.getUpdatedAt(),
                savedMessage.getContent(),
                channel.getId(),
                null,
                List.of()
        );

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
        given(messageRepository.save(any(Message.class))).willReturn(savedMessage);
        given(readStatusRepository.findAllByChannel_IdAndNotificationEnabledTrue(channel.getId()))
                .willReturn(List.of(readStatus));
        given(messageMapper.toDto(eq(savedMessage), anySet())).willReturn(expectedDto);

        // when
        MessageDto result = messageService.create(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(userRepository).should().findById(author.getId());
        then(channelRepository).should().findById(channel.getId());
        then(messageRepository).should().save(any(Message.class));
        then(messageMapper).should().toDto(eq(savedMessage), anySet());

        ArgumentCaptor<MessageCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(MessageCreatedEvent.class);
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        MessageCreatedEvent event = eventCaptor.getValue();
        assertThat(event.message()).isEqualTo(expectedDto);
        assertThat(event.channelName()).isEqualTo(channel.getName());
        assertThat(event.receiverIds()).containsExactly(receiver.getId());
    }

    @Test
    void create_fail_whenAuthorNotFound() {
        // given
        UUID authorId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        MessageCreateRequest request = new MessageCreateRequest(
                authorId,
                channelId,
                "hello",
                null
        );

        given(userRepository.findById(authorId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(authorId);
        then(channelRepository).should(never()).findById(any(UUID.class));
        then(messageRepository).should(never()).save(any(Message.class));
    }

    @Test
    void create_fail_whenChannelNotFound() {
        // given
        User author = new User("evan", "evan@test.com", "password");
        UUID channelId = UUID.randomUUID();

        MessageCreateRequest request = new MessageCreateRequest(
                author.getId(),
                channelId,
                "hello",
                null
        );

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(ChannelNotFoundException.class);

        then(userRepository).should().findById(author.getId());
        then(channelRepository).should().findById(channelId);
        then(messageRepository).should(never()).save(any(Message.class));
    }

    @Test
    void update_success() {
        // given
        User author = new User("evan", "evan@test.com", "password");
        Channel channel = new Channel("general", "general channel");
        Message message = new Message(author, channel, "old content");

        MessageUpdateRequest request = new MessageUpdateRequest("new content");
        MessageUpdateParam param = new MessageUpdateParam(message.getId(), request);

        MessageDto expectedDto = new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                "new content",
                channel.getId(),
                null,
                List.of()
        );

        given(messageRepository.findById(message.getId())).willReturn(Optional.of(message));
        given(messageMapper.toDto(eq(message), anySet())).willReturn(expectedDto);

        // when
        MessageDto result = messageService.update(param);

        // then
        assertThat(result).isEqualTo(expectedDto);
        assertThat(message.getContent()).isEqualTo("new content");

        then(messageRepository).should().findById(message.getId());
        then(messageMapper).should().toDto(eq(message), anySet());
    }

    @Test
    void update_fail_whenMessageNotFound() {
        // given
        UUID messageId = UUID.randomUUID();

        MessageUpdateRequest request = new MessageUpdateRequest("new content");
        MessageUpdateParam param = new MessageUpdateParam(messageId, request);

        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.update(param))
                .isInstanceOf(MessageNotFoundException.class);

        then(messageRepository).should().findById(messageId);
        verifyNoInteractions(messageMapper);
    }

    @Test
    void delete_success() {
        // given
        User author = new User("evan", "evan@test.com", "password");
        Channel channel = new Channel("general", "general channel");
        Message message = new Message(author, channel, "hello");

        given(messageRepository.findById(message.getId())).willReturn(Optional.of(message));

        // when
        messageService.delete(message.getId());

        // then
        then(messageRepository).should().findById(message.getId());
        then(messageRepository).should().deleteById(message.getId());
    }

    @Test
    void delete_fail_whenMessageNotFound() {
        // given
        UUID messageId = UUID.randomUUID();

        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.delete(messageId))
                .isInstanceOf(MessageNotFoundException.class);

        then(messageRepository).should().findById(messageId);
        then(messageRepository).should(never()).deleteById(any(UUID.class));
    }

    @Test
    void findAllByChannelId_success() {
        // given
        User author = new User("evan", "evan@test.com", "password");
        Channel channel = new Channel("general", "general channel");
        Message message = new Message(author, channel, "hello");

        MessageDto messageDto = new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                channel.getId(),
                null,
                List.of()
        );

        given(messageRepository.findPageIdsByChannelIdAndCursor(
                eq(channel.getId()),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).willReturn(List.of(message.getId()));

        given(messageRepository.findAllWithDetailsByIdIn(List.of(message.getId())))
                .willReturn(List.of(message));

        given(messageMapper.toDto(eq(message), anySet())).willReturn(messageDto);

        // when
        PageResponse<MessageDto> result = messageService.findAllByChannelId(
                channel.getId(),
                null,
                50
        );

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(messageDto);
        assertThat(result.nextCursor()).isNull();
        assertThat(result.size()).isEqualTo(50);

        then(messageRepository).should().findPageIdsByChannelIdAndCursor(
                eq(channel.getId()),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
        then(messageRepository).should().findAllWithDetailsByIdIn(List.of(message.getId()));
        then(messageMapper).should().toDto(eq(message), anySet());
    }

    @Test
    void findAllByChannelId_fail_whenCursorInvalid() {
        // given
        UUID channelId = UUID.randomUUID();
        String invalidCursor = "invalid-cursor";

        // when & then
        assertThatThrownBy(() -> messageService.findAllByChannelId(channelId, invalidCursor, 50))
                .isInstanceOf(DiscodeitException.class);

        then(messageRepository).should(never()).findPageIdsByChannelIdAndCursor(
                any(UUID.class),
                any(),
                any(),
                any(Pageable.class)
        );
    }
}
