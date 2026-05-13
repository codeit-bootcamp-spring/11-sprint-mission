package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateDeniedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicChannelServiceTest {

  @InjectMocks
  private BasicChannelService channelService;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelMapper channelMapper;


  @Test
  @DisplayName("PUBLIC 채널 생성 성공")
  void createPublic_success() {
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(
        "코드잇", "스프린트미션7");
    ChannelDto dto = new ChannelDto(UUID.randomUUID(),
        ChannelType.PUBLIC, "코드잇", "스프린트미션7",
        List.of(), null);

    given(channelMapper.toDto(any(Channel.class))).willReturn(dto);

    ChannelDto result = channelService.createPublic(request);

    assertThat(result.name()).isEqualTo("코드잇");
    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("PRIVATE 채널 생성 성공 - 존재하는 유저 참여")
  void createPrivate_success() {
    UUID userId = UUID.randomUUID();
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(userId));
    User existingUser = new User(
        "testUser", "test@email.com", "password");

    UserDto userDto = new UserDto(userId,
        "testUser", "test@email.com", null, true);
    ChannelDto dto = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE,
        null, null, List.of(userDto), null);

    given(channelRepository.save(any(Channel.class))).willAnswer(
        invoction -> invoction.getArgument(0));
    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
    given(channelMapper.toDto(any(Channel.class))).willReturn(dto);

    ChannelDto result = channelService.createPrivate(request);

    assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);
    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("채널 목록 조회 성공")
  void findAllByUserId_success() {
    UUID userId = UUID.randomUUID();
    Channel publicChannel = new Channel(ChannelType.PUBLIC,
        "공용방", "공용입니다");
    Channel privateChannel = new Channel(ChannelType.PRIVATE,
        null, null);

    ReadStatus readStatus = new ReadStatus(new User("유저",
        "이메일", "비밀번호"), privateChannel, null);

    given(readStatusRepository.findByUserId(userId)).willReturn(List.of(readStatus));
    given(channelRepository.findAll()).willReturn(List.of(publicChannel, privateChannel));

    UserDto userDto = new UserDto(userId, "유저", "이메일",
        null, true);
    ChannelDto pubDto = new ChannelDto(publicChannel.getId(),
        ChannelType.PUBLIC, "공용방", "공용입니다",
        List.of(), null);
    ChannelDto privDto = new ChannelDto(privateChannel.getId(),
        ChannelType.PRIVATE, null, null,
        List.of(userDto), null);

    given(channelMapper.toDto(publicChannel)).willReturn(pubDto);
    given(channelMapper.toDto(privateChannel)).willReturn(privDto);

    List<ChannelDto> result = channelService.findAllByUserId(userId);

    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("채널 수정 성공 - PUBLIC 채널만 수정 가능")
  void update_success() {
    UUID channelId = UUID.randomUUID();
    ChannelUpdateRequest request = new ChannelUpdateRequest("바뀐이름", "바뀐설명");
    Channel channel = new Channel(ChannelType.PUBLIC, "원래이름", "원래설명");

    ChannelDto dto = new ChannelDto(channelId, ChannelType.PUBLIC,
        "바뀐이름", "바뀐설명", List.of(), null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(channelMapper.toDto(channel)).willReturn(dto);

    ChannelDto result = channelService.update(channelId, request);

    assertThat(result.name()).isEqualTo("바뀐이름");
  }

  @Test
  @DisplayName("채널 수정 실패 - PRIVATE 채널은 수정 불가")
  void update_fail_privateChannel() {
    UUID channelId = UUID.randomUUID();

    ChannelUpdateRequest request = new ChannelUpdateRequest("이름을 바꿔주세요", "안됨");
    Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

    assertThrows(PrivateChannelUpdateDeniedException.class, () -> {
      channelService.update(channelId, request);
    });
  }

  @Test
  @DisplayName("채널 삭제 성공")
  void delete_success() {
    UUID channelId = UUID.randomUUID();

    channelService.delete(channelId);

    then(channelRepository).should().deleteById(channelId);
  }

  @Test
  @DisplayName("채널 삭제 실패 - DB 연결 예외 발생 시뮬레이션")
  void delete_fail() {
    UUID channelId = UUID.randomUUID();

    willThrow(new RuntimeException("DB 접속 오류")).given(channelRepository).deleteById(channelId);

    assertThrows(RuntimeException.class, () -> {
      channelService.delete(channelId);
    });
  }
}
