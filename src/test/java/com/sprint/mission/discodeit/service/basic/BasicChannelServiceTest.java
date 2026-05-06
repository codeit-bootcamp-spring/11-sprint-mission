package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.projection.ChannelLastMessageAtProjection;
import com.sprint.mission.discodeit.dto.projection.ChannelParticipantProjection;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelParticipantDuplicatedException;
import com.sprint.mission.discodeit.exception.channel.ChannelUpdateNotAllowedException;
import com.sprint.mission.discodeit.exception.channel.InvalidParticipantIdException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class BasicChannelServiceTest {

    @Mock
    private ChannelRepository channelRepo;

    @Mock
    private MessageRepository messageRepo;

    @Mock
    private ReadStatusRepository readStatusRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ChannelMapper channelMapper;

    @Mock
    private BinaryContentService binaryContentService;

    @InjectMocks
    private BasicChannelService channelService;

    @Test
    void createPublicChannel_success() {

        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "general",
                "general channel"
        );

        ChannelDto expected = new ChannelDto(
                UUID.randomUUID(),
                ChannelType.PUBLIC,
                "general",
                "general channel",
                List.of(),
                null
        );

        given(channelMapper.toDto(any(Channel.class), eq(List.of()), isNull()))
                .willReturn(expected);

        ChannelDto result = channelService.createPublicChannel(request);

        assertThat(result).isEqualTo(expected);

        then(channelRepo).should().save(any(Channel.class));
        then(channelMapper).should().toDto(any(Channel.class), eq(List.of()), isNull());
    }

    @Test
    void createPrivateChannel_success() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(userId1, userId2)
        );

        User user1 = new User("user1", "user1@test.com", "password", null);
        User user2 = new User("user2", "user2@test.com", "password", null);
        List<User> participants = List.of(user1, user2);

        ChannelDto expected = new ChannelDto(
                UUID.randomUUID(),
                ChannelType.PRIVATE,
                null,
                null,
                List.of(),
                null
        );

        given(userRepo.findAllById(Set.of(userId1, userId2))).willReturn(participants);
        given(channelMapper.toDto(any(Channel.class), eq(participants), isNull()))
                .willReturn(expected);

        ChannelDto result = channelService.createPrivateChannel(request);

        assertThat(result).isEqualTo(expected);

        then(userRepo).should().findAllById(Set.of(userId1, userId2));
        then(channelRepo).should().save(any(Channel.class));
        then(readStatusRepo).should().saveAll(anyList());
        then(channelMapper).should().toDto(any(Channel.class), eq(participants), isNull());
    }

    @Test
    void createPrivateChannel_fail_whenParticipantDuplicated() {
        UUID userId = UUID.randomUUID();

        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(userId, userId)
        );

        assertThatThrownBy(() -> channelService.createPrivateChannel(request))
                .isInstanceOf(ChannelParticipantDuplicatedException.class);

        then(userRepo).should(never()).findAllById(any());
        then(channelRepo).should(never()).save(any());
        then(readStatusRepo).should(never()).saveAll(any());
    }

    @Test
    void createPrivateChannel_fail_whenInvalidParticipantId() {
        UUID validUserId = UUID.randomUUID();
        UUID invalidUserId = UUID.randomUUID();

        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(validUserId, invalidUserId)
        );

        User validUser = new User("user1", "user1@test.com", "password", null);

        given(userRepo.findAllById(Set.of(validUserId, invalidUserId)))
                .willReturn(List.of(validUser));

        assertThatThrownBy(() -> channelService.createPrivateChannel(request))
                .isInstanceOf(InvalidParticipantIdException.class);

        then(userRepo).should().findAllById(Set.of(validUserId, invalidUserId));
        then(channelRepo).should(never()).save(any());
        then(readStatusRepo).should(never()).saveAll(any());
    }

    @Test
    void updateChannel_success() {
        UUID channelId = UUID.randomUUID();

        Channel channel = new Channel(
                ChannelType.PUBLIC,
                "old name",
                "old description"
        );

        ChannelUpdateRequest request = new ChannelUpdateRequest(
                "new name",
                "new description"
        );

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));

        channelService.update(channelId, request);

        assertThat(channel.getName()).isEqualTo("new name");
        assertThat(channel.getDescription()).isEqualTo("new description");

        then(channelRepo).should().findById(channelId);
    }

    @Test
    void updateChannel_fail_whenChannelNotFound() {
        UUID channelId = UUID.randomUUID();

        ChannelUpdateRequest request = new ChannelUpdateRequest(
                "new name",
                "new description"
        );

        given(channelRepo.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.update(channelId, request))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepo).should().findById(channelId);
    }

    @Test
    void updateChannel_fail_whenPrivateChannel() {
        UUID channelId = UUID.randomUUID();

        Channel channel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );

        ChannelUpdateRequest request = new ChannelUpdateRequest(
                "new name",
                "new description"
        );

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));

        assertThatThrownBy(() -> channelService.update(channelId, request))
                .isInstanceOf(ChannelUpdateNotAllowedException.class);

        then(channelRepo).should().findById(channelId);
    }

    @Test
    void deleteChannel_success() {
        UUID channelId = UUID.randomUUID();

        Channel channel = new Channel(
                ChannelType.PUBLIC,
                "general",
                "general channel"
        );

        User author = new User("user1", "user1@test.com", "password", null);

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

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));
        given(messageRepo.findAllWithAttachmentsByChannel(channel))
                .willReturn(List.of(message));

        channelService.delete(channelId);

        then(channelRepo).should().findById(channelId);
        then(messageRepo).should().findAllWithAttachmentsByChannel(channel);
        then(binaryContentService).should().deleteAll(List.of(attachment));
        then(channelRepo).should().delete(channel);
    }

    @Test
    void deleteChannel_fail_whenChannelNotFound() {
        UUID channelId = UUID.randomUUID();

        given(channelRepo.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.delete(channelId))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepo).should().findById(channelId);
        then(messageRepo).should(never()).findAllWithAttachmentsByChannel(any());
        then(binaryContentService).should(never()).deleteAll(any());
        then(channelRepo).should(never()).delete(any());
    }

    @Test
    void findAllByUserId_success() {
        UUID userId = UUID.randomUUID();

        User user = new User("user1", "user1@test.com", "password", null);
        User participant = new User("user2", "user2@test.com", "password", null);

        Channel publicChannel = new Channel(
                ChannelType.PUBLIC,
                "general",
                "general channel"
        );

        Channel privateChannel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );

        Instant publicLastMessageAt = Instant.now();
        Instant privateLastMessageAt = Instant.now();

        ChannelDto publicChannelDto = new ChannelDto(
                publicChannel.getId(),
                ChannelType.PUBLIC,
                "general",
                "general channel",
                List.of(),
                publicLastMessageAt
        );

        ChannelDto privateChannelDto = new ChannelDto(
                privateChannel.getId(),
                ChannelType.PRIVATE,
                null,
                null,
                List.of(),
                privateLastMessageAt
        );

        List<Channel> allChannels = List.of(publicChannel, privateChannel);
        List<Channel> privateChannels = List.of(privateChannel);

        given(userRepo.findById(userId)).willReturn(Optional.of(user));
        given(channelRepo.findAllByChannelType(ChannelType.PUBLIC))
                .willReturn(List.of(publicChannel));
        given(channelRepo.findChannelsByUser(user))
                .willReturn(privateChannels);
        given(messageRepo.findLastMessageAtByChannels(allChannels))
                .willReturn(List.of(
                        new ChannelLastMessageAtProjection(publicChannel.getId(), publicLastMessageAt),
                        new ChannelLastMessageAtProjection(privateChannel.getId(), privateLastMessageAt)
                ));
        given(readStatusRepo.findUsersByChannels(privateChannels))
                .willReturn(List.of(
                        new ChannelParticipantProjection(privateChannel.getId(), participant)
                ));
        given(channelMapper.toDto(publicChannel, List.of(), publicLastMessageAt))
                .willReturn(publicChannelDto);
        given(channelMapper.toDto(privateChannel, List.of(participant), privateLastMessageAt))
                .willReturn(privateChannelDto);

        List<ChannelDto> result = channelService.findAllByUserId(userId);

        assertThat(result).containsExactly(publicChannelDto, privateChannelDto);

        then(userRepo).should().findById(userId);
        then(channelRepo).should().findAllByChannelType(ChannelType.PUBLIC);
        then(channelRepo).should().findChannelsByUser(user);
        then(messageRepo).should().findLastMessageAtByChannels(allChannels);
        then(readStatusRepo).should().findUsersByChannels(privateChannels);
    }

    @Test
    void findAllByUserId_fail_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        given(userRepo.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.findAllByUserId(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepo).should().findById(userId);
        then(channelRepo).should(never()).findAllByChannelType(any());
        then(channelRepo).should(never()).findChannelsByUser(any());
    }

}
