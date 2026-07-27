package com.sprint.mission.discodeit.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class ChannelServiceTest {

  @Mock
  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private ChannelMapper channelMapper;

  @Mock
  private CacheManager cacheManager;

  @InjectMocks
  private BasicChannelService channelService;

  @Test
  @DisplayName("공개 채널 생성 성공")
  void createPublic_success() {
    // given
    ChannelCreatePublicRequest request = new ChannelCreatePublicRequest("새 채널", "새로운 채널입니다.");

    given(channelRepository.save(any(Channel.class))).willAnswer(i -> i.getArgument(0));

    given(channelMapper.toDto(any(Channel.class), anyList(), any())).willAnswer(i -> {
      Channel c = i.getArgument(0);

      return new ChannelDto(
          c.getId(),
          c.getType(),
          c.getName(),
          c.getDescription(),
          List.of(),
          Instant.now()
      );
    });

    // when
    ChannelDto result = channelService.createPublic(request);

    // then
    assertThat(result).isNotNull();
    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("비공개 채널 생성 성공")
  void createPrivate_success() {
    // given
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    User user1 = mock(User.class);
    User user2 = mock(User.class);

    ChannelCreatePrivateRequest request = new ChannelCreatePrivateRequest(
        List.of(userId1, userId2));

    List<User> users = List.of(
        User.create("user1", "test1@naver.com", "1234"),
        User.create("user2", "test2@naver.com", "1234")
    );

    List<UserDto> dto = List.of(
        new UserDto(userId1, "user1", "test1@naver.com", null, true, Role.USER),
        new UserDto(userId1, "user2", "test2@naver.com", null, true, Role.USER)
    );

    given(channelRepository.save(any(Channel.class))).willAnswer(i -> i.getArgument(0));

    given(userRepository.findAllById(request.participantIds())).willReturn(users);

    given(readStatusRepository.save(any())).willAnswer(i -> i.getArgument(0));

    given(channelMapper.toDto(any(Channel.class), anyList(), any())).willAnswer(i -> {
      Channel c = i.getArgument(0);

      return new ChannelDto(
          c.getId(),
          c.getType(),
          c.getName(),
          c.getDescription(),
          dto,
          Instant.now()
      );
    });

    // when
    ChannelDto result = channelService.createPrivate(request);

    // then
    assertThat(result).isNotNull();

    then(channelRepository).should().save(any(Channel.class));
    then(userRepository).should().findAllById(request.participantIds());
    then(readStatusRepository).should(times(2)).save(any()); // 2명의 User에 대한 2번 호출
  }

  @Test
  @DisplayName("비공개 채널 생성 실패(참여자가 없음)")
  void createPrivate_fail_emptyParticipantIds() {
    // given
    UUID userId = UUID.randomUUID();
    ChannelCreatePrivateRequest request = new ChannelCreatePrivateRequest(List.of(userId));

    given(channelRepository.save(any())).willAnswer(i -> i.getArgument(0));

    // 유저 없음
    given(userRepository.findAllById(any())).willReturn(List.of());

    given(channelMapper.toDto(any(Channel.class), anyList(), any())).willAnswer(i -> {
      Channel c = i.getArgument(0);

      return new ChannelDto(
          c.getId(),
          c.getType(),
          c.getName(),
          c.getDescription(),
          List.of(),
          Instant.now()
      );
    });

    // when
    ChannelDto result = channelService.createPrivate(request);

    // then
    assertThat(result).isNotNull();
    then(readStatusRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("채널 수정 성공")
  void update_success_channelNameAndDescription() {
    // given
    UUID channelId = UUID.randomUUID();

    Channel channel = Channel.createPublic("채널", "채널입니다.");

    ChannelUpdateRequest request = new ChannelUpdateRequest("새 채널", "새 채널입니다.");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    given(channelMapper.toDto(any(Channel.class), anyList(), any())).willAnswer(i -> {
      Channel c = i.getArgument(0);

      return new ChannelDto(
          c.getId(),
          c.getType(),
          c.getName(),
          c.getDescription(),
          List.of(),
          Instant.now()
      );
    });

    // when
    ChannelDto result = channelService.update(channelId, request);

    // then
    assertThat(result.name()).isEqualTo("새 채널");
    assertThat(result.description()).isEqualTo("새 채널입니다.");
  }

  @Test
  @DisplayName("채널 수정 실패(채널이 존재하지 않음)")
  void update_fail_notfound_channel() {
    // given
    UUID channelId = UUID.randomUUID();

    ChannelUpdateRequest request = new ChannelUpdateRequest("새 채널", "새 채널입니다.");

    // 채널ID를 가진 채널이 없음
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> channelService.update(channelId, request)).isInstanceOf(
        DiscodeitException.class);

    then(channelRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("채널 수정 실패(비공개 채널은 수정할 수 없음)")
  void update_privateChannel_fail_notallowed_channel() {
    // given
    UUID channelId = UUID.randomUUID();

    Channel privateChannel = Channel.createPrivate();
    ChannelUpdateRequest request = new ChannelUpdateRequest("공개", "공개 채널입니다.");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

    // when & then
    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(DiscodeitException.class);

    then(channelRepository).should().findById(channelId);
    then(channelRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("채널 삭제 성공")
  void delete_success_channel() {
    // given
    UUID channelId = UUID.randomUUID();

    Channel channel = Channel.createPublic("채널", "채널입니다.");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    // when
    channelService.delete(channelId);

    // then
    then(messageRepository).should().deleteAllByChannelId(channelId);
    then(readStatusRepository).should().deleteAllByChannelId(channelId);
    then(channelRepository).should().deleteById(channelId);
  }

  @Test
  @DisplayName("채널 삭제 실패(채널이 존재하지 않음)")
  void delete_fail_notfound_channel() {
    // given
    UUID channelId = UUID.randomUUID();

    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> channelService.delete(channelId)).isInstanceOf(
        DiscodeitException.class);

    then(messageRepository).should(never()).deleteAllByChannelId(channelId);
    then(readStatusRepository).should(never()).deleteAllByChannelId(channelId);
    then(channelRepository).should(never()).deleteById(channelId);
  }

  @Test
  @DisplayName("유저ID로 채널 조회 성공")
  void findAllByUserId_Success() {
    // given
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    given(user.getId()).willReturn(userId);

    UUID publicChannelId = UUID.randomUUID();
    UUID privateChannelId = UUID.randomUUID();
    Channel publicChannel = mock(Channel.class);
    Channel privateChannel = mock(Channel.class);
    given(publicChannel.getId()).willReturn(publicChannelId);
    given(privateChannel.getId()).willReturn(privateChannelId);

    given(channelRepository.findAll()).willReturn(List.of(publicChannel, privateChannel));

    ReadStatus readStatus = mock(ReadStatus.class);
    given(readStatus.getChannel()).willReturn(privateChannel);
    given(readStatus.getUser()).willReturn(user);

    given(readStatusRepository.findByChannelIdIn(anyList())).willReturn(List.of(readStatus));

    Message message1 = mock(Message.class);
    Message message2 = mock(Message.class);
    given(message1.getChannel()).willReturn(publicChannel);
    given(message1.getCreatedAt()).willReturn(Instant.now());
    given(message2.getChannel()).willReturn(privateChannel);
    given(message2.getCreatedAt()).willReturn(Instant.now());

    given(messageRepository.findLastMessagesByChannelIds(anyList()))
        .willReturn(List.of(message1, message2));

    given(channelMapper.toDto(any(), anyList(), any())).willReturn(mock(ChannelDto.class));

    // when
    List<ChannelDto> result = channelService.findAllByUserId(userId);

    // then
    assertThat(result).isNotEmpty();

    then(channelRepository).should().findAll();
    then(readStatusRepository).should().findByChannelIdIn(anyList());
    then(messageRepository).should().findLastMessagesByChannelIds(anyList());
  }

  @Test
  @DisplayName("유저ID로 채널 조회 실패(비공개 채널의 참여자가 없음)")
  void findAllByUserId_fail_emptyParticipantIds() {
    UUID userId = UUID.randomUUID();

    Channel privateChannel = Channel.createPrivate();

    given(channelRepository.findAll()).willReturn(List.of(privateChannel));

    // 참여자 없음
    given(readStatusRepository.findByChannelIdIn(anyList())).willReturn(List.of());

    given(messageRepository.findLastMessagesByChannelIds(anyList())).willReturn(List.of());

    // when
    List<ChannelDto> result = channelService.findAllByUserId(userId);

    // then
    assertThat(result).isEmpty();
  }
}
