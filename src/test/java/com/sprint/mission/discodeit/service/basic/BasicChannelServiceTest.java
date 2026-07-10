package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
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
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private ChannelMapper channelMapper;

  @InjectMocks
  private BasicChannelService channelService;

  private UUID channelId;
  private UUID userId;
  private Channel mockPublicChannel;
  private Channel mockPrivateChannel;
  private User mockUser;
  private UserDto mockUserDto;
  private ChannelDto mockChannelDto;

  @BeforeEach
  void setUp() {
    channelId = UUID.randomUUID();
    userId = UUID.randomUUID();

    mockPublicChannel = new Channel("테스트", "테스트 채널입니다.", ChannelType.PUBLIC);
    ReflectionTestUtils.setField(mockPublicChannel, "id", channelId);

    mockPrivateChannel = new Channel("Private Channel", "", ChannelType.PRIVATE);
    ReflectionTestUtils.setField(mockPrivateChannel, "id", channelId);

    mockUser = new User("tester", "test@test.com", "password123", null);

    mockUserDto = new UserDto(userId, "tester", "test@test.com", null, true, Role.USER);
    mockChannelDto = new ChannelDto(channelId, "테스트", ChannelType.PUBLIC, "테스트 채널입니다.",
        Instant.now(),
        Collections.emptyList());
  }

  @Nested
  @DisplayName("createPublicChannel()")
  class CreatePublicChannel {

    // --- 성공 ---
    @Test
    @DisplayName("Public 채널 생성")
    void createPublicChannel_success() {
      // given
      PublicChannelRequest request = new PublicChannelRequest("테스트", "테스트입니다.");

      given(channelRepository.save(any(Channel.class))).willReturn(mockPublicChannel);
      given(channelMapper.toDto(any(Channel.class), nullable(Instant.class), any())).willReturn(
          mockChannelDto);

      // when
      ChannelDto result = channelService.createPublicChannel(request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.type()).isEqualTo(ChannelType.PUBLIC);
      then(channelRepository).should().save(any(Channel.class));
    }

    // --- 실패 ---
    @Test
    @DisplayName("채널 저장 중 예외가 발생하면 그대로 전파됨")
    void createPublicChannel_repositoryThrows_exception() {
      // given
      PublicChannelRequest request = new PublicChannelRequest("테스트", "테스트 채널입니다.");

      given(channelRepository.save(any(Channel.class))).willThrow(new RuntimeException("DB 저장실패"));

      // when & then
      assertThatThrownBy(() -> channelService.createPublicChannel(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("DB 저장실패");
    }
  }

  @Nested
  @DisplayName("createPrivateChannel()")
  class CreatePrivateChannel {

    // --- 성공 ---
    @Test
    @DisplayName("참여자 목록을 포함한 private 채널 생성")
    void createPrivateChannel_success() {
      // given
      UUID participantId = UUID.randomUUID();
      PrivateChannelRequest request = new PrivateChannelRequest(List.of(participantId));

      ChannelDto privateChannelDto = new ChannelDto(channelId, "Private Channel",
          ChannelType.PRIVATE, "", Instant.now(), List.of(mockUserDto));

      given(channelRepository.save(any(Channel.class))).willReturn(mockPrivateChannel);
      given(userRepository.findById(participantId)).willReturn(Optional.of(mockUser));
      given(readStatusRepository.save(any(ReadStatus.class))).willReturn(mock(ReadStatus.class));
      given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(mockUserDto);
      given(channelMapper.toDto(any(Channel.class), nullable(Instant.class), any())).willReturn(
          privateChannelDto);

      // when
      ChannelDto result = channelService.createPrivateChannel(request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);
      then(readStatusRepository).should().save(any(ReadStatus.class));
    }

    // --- 실패 ---
    @Test
    @DisplayName("참여자 중 존재하지 않는 userId가 있는 경우 UserNotFound 예외가 발생")
    void createPrivateChannel_userNotFound_throwsUserNotFoundException() {
      // given
      UUID unknownId = UUID.randomUUID();
      PrivateChannelRequest request = new PrivateChannelRequest(List.of(unknownId));

      given(channelRepository.save(any(Channel.class))).willReturn(mockPrivateChannel);
      given(userRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.createPrivateChannel(request))
          .isInstanceOf(UserNotFoundException.class);
      then(readStatusRepository).should(never()).save(any());
    }
  }

  @Nested
  @DisplayName("findAllByUserId()")
  class FindAllByUserId {

    // --- 성공 ---
    @Test
    @DisplayName("Public 채널은 모두 유저가 반환됨")
    void findAllByUserId_publicChannelIncluded() {
      // given
      given(channelRepository.findAll()).willReturn(List.of(mockPublicChannel));
      given(readStatusRepository.findAllByUserId(userId)).willReturn(Collections.emptyList());
      given(messageRepository.findLatestMessagesByChannelIds(any())).willReturn(
          Collections.emptyList());
      given(readStatusRepository.findAllByChannelIdIn(any())).willReturn(Collections.emptyList());
      given(channelMapper.toDto(any(Channel.class), nullable(Instant.class), any())).willReturn(
          mockChannelDto);

      // when
      List<ChannelDto> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).type()).isEqualTo(ChannelType.PUBLIC);
    }

    @Test
    @DisplayName("Private 채널은 참여하지 않으면 반환되지 않음")
    void findAllByUserId_privateChannel_notParticipant() {
      // given
      given(channelRepository.findAll()).willReturn(List.of(mockPrivateChannel));
      given(readStatusRepository.findAllByUserId(userId)).willReturn(Collections.emptyList());
      given(messageRepository.findLatestMessagesByChannelIds(any())).willReturn(
          Collections.emptyList());
      given(readStatusRepository.findAllByChannelIdIn(any())).willReturn(Collections.emptyList());

      // when
      List<ChannelDto> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("deleteChannel()")
  class DeleteChannel {

    // --- 성공 ---
    @Test
    @DisplayName("채널이 정상적으로 삭제됨")
    void deleteChannel_success() {
      // given
      given(channelRepository.findById(channelId)).willReturn(Optional.of(mockPublicChannel));

      // when
      channelService.deleteChannel(channelId);

      // then
      then(channelRepository).should().delete(mockPublicChannel);
    }

    // --- 실패 ---
    @Test
    @DisplayName("존재하지 않는 channelId일 경우 ChannelNotFoundException이 발생")
    void deleteChannel_channelNotFound_throwsChannelNotFoundException() {
      // given
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.deleteChannel(channelId))
          .isInstanceOf(ChannelNotFoundException.class);
      then(channelRepository).should(never()).delete(any());
    }
  }

  @Nested
  @DisplayName("updateChannel()")
  class UpdateChannel {

    // --- 성공 ---
    @Test
    @DisplayName("채널 이름과 설명 변경성공")
    void updateChannel_success() {
      // given
      ChannelUpdateRequest request = new ChannelUpdateRequest("new", "new");
      ChannelDto updateDto = new ChannelDto(channelId, "new", ChannelType.PUBLIC, "new",
          Instant.now(), Collections.emptyList());

      given(channelRepository.findById(channelId)).willReturn(Optional.of(mockPublicChannel));
      given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(channelId))
          .willReturn(Optional.empty());
      given(channelMapper.toDto(any(Channel.class), nullable(Instant.class), any()))
          .willReturn(updateDto);

      // when
      ChannelDto result = channelService.updateChannel(channelId, request);

      // then
      assertThat(result.name()).isEqualTo("new");
      assertThat(result.description()).isEqualTo("new");
    }

    // --- 실패 ---
    @Test
    @DisplayName("channelId가 존재하지 않을 경우 ChannelNotFoundException이 발생")
    void updateChannel_channelNotFound_throwsChannelNotFoundException() {
      // given
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(
          () -> channelService.updateChannel(channelId, new ChannelUpdateRequest("test", "test")))
          .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("private 채널을 수정할 경우 PrivateChannelUpdateException 발생")
    void updateChannel_privateChannel_throwsPrivateChannelUpdateException() {
      // given
      given(channelRepository.findById(channelId)).willReturn(Optional.of(mockPrivateChannel));

      // when & then
      assertThatThrownBy(
          () -> channelService.updateChannel(channelId, new ChannelUpdateRequest("test", "test")))
          .isInstanceOf(PrivateChannelUpdateException.class);
    }
  }
}