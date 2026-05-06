package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class BasicMessageServiceTest {

    @Mock
    private MessageRepository messageRepo;

    @Mock
    private ChannelRepository channelRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private PageResponseMapper pageResponseMapper;

    @Mock
    private BinaryContentService binaryContentService;

    @Mock
    private MultipartFile attachment;

    @InjectMocks
    private BasicMessageService messageService;

    @Test
    void create_success() {
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                channelId,
                authorId
        );

        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        User author = new User("taehk23", "taehk23@test.com", "password", null);

        BinaryContent binaryContent = new BinaryContent(
                "image.png",
                "image/png",
                1000L
        );

        List<MultipartFile> attachments = List.of(attachment);
        List<BinaryContent> binaryContents = List.of(binaryContent);

        MessageDto expected = new MessageDto(
                UUID.randomUUID(),
                Instant.now(),
                Instant.now(),
                "hello",
                channelId,
                null,
                List.of()
        );

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepo.findById(authorId)).willReturn(Optional.of(author));
        given(binaryContentService.createAll(attachments)).willReturn(binaryContents);
        given(messageMapper.toDto(any(Message.class))).willReturn(expected);

        MessageDto result = messageService.create(request, attachments);

        assertThat(result).isEqualTo(expected);

        then(channelRepo).should().findById(channelId);
        then(userRepo).should().findById(authorId);
        then(binaryContentService).should().createAll(attachments);
        then(messageRepo).should().save(any(Message.class));
        then(messageMapper).should().toDto(any(Message.class));
    }

    @Test
    void create_fail_whenChannelNotFound() {
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                channelId,
                authorId
        );

        List<MultipartFile> attachments = List.of(attachment);

        given(channelRepo.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(request, attachments))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepo).should().findById(channelId);
        then(userRepo).should(never()).findById(any());
        then(binaryContentService).should(never()).createAll(any());
        then(messageRepo).should(never()).save(any());
    }

    @Test
    void create_fail_whenUserNotFound() {
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                channelId,
                authorId
        );

        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        List<MultipartFile> attachments = List.of(attachment);

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepo.findById(authorId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(request, attachments))
                .isInstanceOf(UserNotFoundException.class);

        then(channelRepo).should().findById(channelId);
        then(userRepo).should().findById(authorId);
        then(binaryContentService).should(never()).createAll(any());
        then(messageRepo).should(never()).save(any());
    }

    @Test
    void update_success() {
        UUID messageId = UUID.randomUUID();

        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        User author = new User("taehk23", "taehk23@test.com", "password", null);
        Message message = new Message("old content", channel, author, List.of());

        MessageUpdateRequest request = new MessageUpdateRequest("new content");

        given(messageRepo.findById(messageId)).willReturn(Optional.of(message));

        messageService.update(messageId, request);

        assertThat(message.getContent()).isEqualTo("new content");

        then(messageRepo).should().findById(messageId);
    }

    @Test
    void update_fail_whenMessageNotFound() {
        UUID messageId = UUID.randomUUID();

        MessageUpdateRequest request = new MessageUpdateRequest("new content");

        given(messageRepo.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.update(messageId, request))
                .isInstanceOf(MessageNotFoundException.class);

        then(messageRepo).should().findById(messageId);
    }

    @Test
    void delete_success() {
        UUID messageId = UUID.randomUUID();

        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        User author = new User("taehk23", "taehk23@test.com", "password", null);

        BinaryContent attachment = new BinaryContent(
                "image.png",
                "image/png",
                1000L
        );

        Message message = new Message(
                "hello",
                channel,
                author,
                List.of(attachment)
        );

        given(messageRepo.findWithDetailsById(messageId)).willReturn(Optional.of(message));

        messageService.delete(messageId);

        then(messageRepo).should().findWithDetailsById(messageId);
        then(binaryContentService).should().deleteAll(List.of(attachment));
        then(messageRepo).should().delete(message);
    }

    @Test
    void delete_fail_whenMessageNotFound() {
        UUID messageId = UUID.randomUUID();

        given(messageRepo.findWithDetailsById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.delete(messageId))
                .isInstanceOf(MessageNotFoundException.class);

        then(messageRepo).should().findWithDetailsById(messageId);
        then(binaryContentService).should(never()).deleteAll(any());
        then(messageRepo).should(never()).delete(any());
    }

    @Test
    void findAllByChannelId_success() {
        UUID channelId = UUID.randomUUID();
        Instant cursor = Instant.now();

        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        User author = new User("taehk23", "taehk23@test.com", "password", null);

        Message message = new Message("hello", channel, author, List.of());
        List<Message> messages = List.of(message);

        MessageDto messageDto = new MessageDto(
                message.getId(),
                Instant.now(),
                Instant.now(),
                "hello",
                channelId,
                null,
                List.of()
        );

        PageResponse<MessageDto> expected = new PageResponse<>(
                List.of(messageDto),
                null,
                1,
                false,
                null
        );

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));
        given(messageRepo.findAllByChannelWithCursor(channel, cursor, 50))
                .willReturn(messages);
        given(pageResponseMapper.toCursorDto(
                eq(messages),
                eq(50),
                any(Function.class),
                any(Function.class)
        )).willReturn(expected);

        PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, cursor);

        assertThat(result).isEqualTo(expected);

        then(channelRepo).should().findById(channelId);
        then(messageRepo).should().findAllByChannelWithCursor(channel, cursor, 50);
        then(pageResponseMapper).should().toCursorDto(
                eq(messages),
                eq(50),
                any(Function.class),
                any(Function.class)
        );
    }

    @Test
    void findAllByChannelId_fail_whenChannelNotFound() {
        UUID channelId = UUID.randomUUID();
        Instant cursor = Instant.now();

        given(channelRepo.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.findAllByChannelId(channelId, cursor))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepo).should().findById(channelId);
        then(messageRepo).should(never()).findAllByChannelWithCursor(any(), any(), anyInt());
        then(pageResponseMapper).should(never()).toCursorDto(any(), anyInt(), any(), any());
    }
}
