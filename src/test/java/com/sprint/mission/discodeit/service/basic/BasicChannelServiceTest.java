package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateParam;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock
    ChannelRepository channelRepository;

    @Mock
    MessageRepository messageRepository;

    @Mock
    ReadStatusRepository readStatusRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ChannelMapper channelMapper;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    BasicChannelService channelService;

    @Test
    void createPublic_success() {
        // given
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "general",
                "general channel"
        );

        Channel savedChannel = new Channel(request.name(), request.description());

        ChannelDto expectedDto = new ChannelDto(
                savedChannel.getId(),
                savedChannel.getName(),
                savedChannel.getDescription(),
                ChannelType.PUBLIC,
                List.of(),
                null
        );

        given(channelRepository.save(any(Channel.class))).willReturn(savedChannel);
        given(channelMapper.toDto(eq(savedChannel), anyList(), isNull())).willReturn(expectedDto);

        // when
        ChannelDto result = channelService.createPublic(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(channelRepository).should().save(any(Channel.class));
        then(channelMapper).should().toDto(eq(savedChannel), anyList(), isNull());
    }

    @Test
    void createPrivate_success() {
        // given
        User user1 = new User("evan", "evan@test.com", "password");
        User user2 = new User("kim", "kim@test.com", "password");

        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(user1.getId(), user2.getId())
        );

        Channel savedChannel = Channel.createPrivateChannel();

        UserDto userDto1 = new UserDto(user1.getId(), user1.getUsername(), user1.getEmail(), null, false);
        UserDto userDto2 = new UserDto(user2.getId(), user2.getUsername(), user2.getEmail(), null, false);

        ChannelDto expectedDto = new ChannelDto(
                savedChannel.getId(),
                null,
                null,
                ChannelType.PRIVATE,
                List.of(userDto1, userDto2),
                null
        );

        given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));
        given(channelRepository.save(any(Channel.class))).willReturn(savedChannel);
        given(userMapper.toDto(user1)).willReturn(userDto1);
        given(userMapper.toDto(user2)).willReturn(userDto2);
        given(channelMapper.toDto(eq(savedChannel), anyList(), isNull())).willReturn(expectedDto);

        // when
        ChannelDto result = channelService.createPrivate(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(userRepository).should().findById(user1.getId());
        then(userRepository).should().findById(user2.getId());
        then(channelRepository).should().save(any(Channel.class));
        then(readStatusRepository).should(times(2)).save(any(ReadStatus.class));
        then(channelMapper).should().toDto(eq(savedChannel), anyList(), isNull());
    }

    @Test
    void createPrivate_fail_whenParticipantNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(userId)
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.createPrivate(request))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(userId);
        then(channelRepository).should(never()).save(any(Channel.class));
        verifyNoInteractions(channelMapper);
    }

    @Test
    void update_success() {
        // given
        Channel channel = new Channel("old", "old description");

        ChannelUpdateRequest request = new ChannelUpdateRequest(
                "new",
                "new description"
        );

        ChannelUpdateParam param = new ChannelUpdateParam(channel.getId(), request);

        ChannelDto expectedDto = new ChannelDto(
                channel.getId(),
                "new",
                "new description",
                ChannelType.PUBLIC,
                List.of(),
                null
        );

        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
        given(messageRepository.findLastMessageTimesByChannelIds(List.of(channel.getId()))).willReturn(List.of());
        given(readStatusRepository.findAllWithUserByChannelIds(List.of(channel.getId()))).willReturn(List.of());
        given(channelMapper.toDto(eq(channel), anyList(), isNull())).willReturn(expectedDto);

        // when
        ChannelDto result = channelService.update(param);

        // then
        assertThat(result).isEqualTo(expectedDto);
        assertThat(channel.getName()).isEqualTo("new");
        assertThat(channel.getDescription()).isEqualTo("new description");

        then(channelRepository).should().findById(channel.getId());
        then(channelMapper).should().toDto(eq(channel), anyList(), isNull());
    }

    @Test
    void update_fail_whenChannelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();

        ChannelUpdateRequest request = new ChannelUpdateRequest(
                "new",
                "new description"
        );

        ChannelUpdateParam param = new ChannelUpdateParam(channelId, request);

        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.update(param))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepository).should().findById(channelId);
        verifyNoInteractions(channelMapper);
    }

    @Test
    void update_fail_whenPrivateChannel() {
        // given
        Channel privateChannel = Channel.createPrivateChannel();

        ChannelUpdateRequest request = new ChannelUpdateRequest(
                "new",
                "new description"
        );

        ChannelUpdateParam param = new ChannelUpdateParam(privateChannel.getId(), request);

        given(channelRepository.findById(privateChannel.getId())).willReturn(Optional.of(privateChannel));

        // when & then
        assertThatThrownBy(() -> channelService.update(param))
                .isInstanceOf(PrivateChannelUpdateException.class);

        then(channelRepository).should().findById(privateChannel.getId());
        verifyNoInteractions(channelMapper);
    }

    @Test
    void delete_success() {
        // given
        Channel channel = new Channel("general", "general channel");

        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));

        // when
        channelService.delete(channel.getId());

        // then
        then(channelRepository).should().findById(channel.getId());
        then(channelRepository).should().delete(channel);
    }

    @Test
    void delete_fail_whenChannelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();

        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.delete(channelId))
                .isInstanceOf(ChannelNotFoundException.class);

        then(channelRepository).should().findById(channelId);
        then(channelRepository).should(never()).delete(any(Channel.class));
    }

    @Test
    void findAllByUserId_empty() {
        // given
        UUID userId = UUID.randomUUID();

        given(channelRepository.findVisibleChannelsByUserId(userId)).willReturn(List.of());

        // when
        List<ChannelDto> result = channelService.findAllByUserId(userId);

        // then
        assertThat(result).isEmpty();

        then(channelRepository).should().findVisibleChannelsByUserId(userId);
    }

    @Test
    void findAllByUserId_success() {
        // given
        UUID userId = UUID.randomUUID();
        Channel channel = new Channel("general", "general channel");

        ChannelDto channelDto = new ChannelDto(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                ChannelType.PUBLIC,
                List.of(),
                Instant.now()
        );

        List<Object[]> lastMessageRows = new ArrayList<>();
        lastMessageRows.add(new Object[]{channel.getId(), channelDto.lastMessageAt()});

        given(channelRepository.findVisibleChannelsByUserId(userId)).willReturn(List.of(channel));
        given(channelMapper.toDto(eq(channel), anyList(), eq(channelDto.lastMessageAt()))).willReturn(channelDto);

        // when
        List<ChannelDto> result = channelService.findAllByUserId(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(channelDto);

        then(channelRepository).should().findVisibleChannelsByUserId(userId);
        then(channelMapper).should().toDto(eq(channel), anyList(), eq(channelDto.lastMessageAt()));
    }
}