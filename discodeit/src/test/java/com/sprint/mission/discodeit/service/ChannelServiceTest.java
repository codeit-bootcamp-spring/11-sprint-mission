package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.service.dto.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.UpdateChannelRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock private ChannelRepository channelRepository;
    @Mock private UserRepository userRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ReadStatusRepository readStatusRepository;
    @Mock private ChannelMapper channelMapper;

    @InjectMocks private ChannelService channelService;

    private Channel publicChannel;
    private Channel privateChannel;
    private User user;
    private ChannelDto channelDto;

    @BeforeEach
    void setUp() {
        publicChannel = Channel.publicChannel("일반", "일반 채널");
        privateChannel = Channel.privateChannel();
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();
        channelDto = ChannelDto.builder()
                .id(publicChannel.getId())
                .type(ChannelType.PUBLIC)
                .name("일반")
                .description("일반 채널")
                .participants(List.of())
                .build();
    }

    @Test
    void createPublicChannel_성공() {
        CreatePublicChannelRequest request = new CreatePublicChannelRequest("일반", "일반 채널");

        given(channelRepository.save(any(Channel.class))).willReturn(publicChannel);
        given(channelMapper.toDto(publicChannel)).willReturn(channelDto);

        ChannelDto result = channelService.createPublicChannel(request);

        assertThat(result).isEqualTo(channelDto);
        then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    void createPublicChannel_이름_없음_예외() {
        CreatePublicChannelRequest request = new CreatePublicChannelRequest("", "설명");

        assertThatThrownBy(() -> channelService.createPublicChannel(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void createPrivateChannel_성공() {
        UUID userId = user.getId();
        CreatePrivateChannelRequest request = new CreatePrivateChannelRequest(List.of(userId));
        ChannelDto privateDto = ChannelDto.builder()
                .id(privateChannel.getId())
                .type(ChannelType.PRIVATE)
                .participants(List.of())
                .build();

        given(channelRepository.save(any(Channel.class))).willReturn(privateChannel);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(readStatusRepository.save(any())).willReturn(null);
        given(channelMapper.toDto(privateChannel)).willReturn(privateDto);

        ChannelDto result = channelService.createPrivateChannel(request);

        assertThat(result).isEqualTo(privateDto);
    }

    @Test
    void createPrivateChannel_참여자_없음_예외() {
        CreatePrivateChannelRequest request = new CreatePrivateChannelRequest(List.of());

        assertThatThrownBy(() -> channelService.createPrivateChannel(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void createPrivateChannel_존재하지_않는_참여자_예외() {
        UUID unknownId = UUID.randomUUID();
        CreatePrivateChannelRequest request = new CreatePrivateChannelRequest(List.of(unknownId));

        given(channelRepository.save(any(Channel.class))).willReturn(privateChannel);
        given(userRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.createPrivateChannel(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_성공() {
        UUID channelId = publicChannel.getId();
        UpdateChannelRequest request = new UpdateChannelRequest(channelId, "수정된채널", "수정된설명");

        given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
        given(channelMapper.toDto(publicChannel)).willReturn(channelDto);

        ChannelDto result = channelService.update(request);

        assertThat(result).isEqualTo(channelDto);
    }

    @Test
    void update_비공개_채널_예외() {
        UUID channelId = privateChannel.getId();
        UpdateChannelRequest request = new UpdateChannelRequest(channelId, "수정시도", null);

        given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

        assertThatThrownBy(() -> channelService.update(request))
                .isInstanceOf(PrivateChannelUpdateException.class);
    }

    @Test
    void update_채널_없음_예외() {
        UUID channelId = UUID.randomUUID();
        UpdateChannelRequest request = new UpdateChannelRequest(channelId, "수정시도", null);

        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.update(request))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    void delete_성공() {
        UUID channelId = publicChannel.getId();

        given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
        given(messageRepository.findAllByChannelId(channelId)).willReturn(List.of());

        channelService.delete(channelId);

        then(channelRepository).should().delete(publicChannel);
    }

    @Test
    void delete_채널_없음_예외() {
        UUID channelId = UUID.randomUUID();
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.delete(channelId))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    void findAllByUserId_성공() {
        UUID userId = user.getId();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());
        given(channelRepository.findAll()).willReturn(List.of(publicChannel));
        given(channelMapper.toDto(publicChannel)).willReturn(channelDto);

        List<ChannelDto> result = channelService.findAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(channelDto);
    }

    @Test
    void findAllByUserId_사용자_없음_예외() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> channelService.findAllByUserId(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
