package com.sprint.mission.discodeit.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;


import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateDeniedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
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
public class ChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private ChannelMapper channelMapper;

  @InjectMocks
  private BasicChannelService channelService;

  @Test
  @DisplayName("PUBLIC 채널 생성 성공")
  void createPublic_success() {
    //given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("공지방", "공지방");
    ChannelDto dto = new ChannelDto(UUID.randomUUID(), "공지방", "공지방", ChannelType.PUBLIC, null,
        null);

    given(channelRepository.save(any(Channel.class))).will(returnsFirstArg());
    given(channelMapper.toDto(any(Channel.class))).willReturn(dto);

    //when
    ChannelDto result = channelService.createPublicChannel(request);

    //then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo(request.name());
  }

  @Test
  @DisplayName("Private 채널 생성 성공")
  void createPrivate_success() {
    //given
    List<UUID> uuidList = List.of(UUID.randomUUID(), UUID.randomUUID());
    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(uuidList);

    User user1 = new User("이마크", "mark@test.com", "pass1234", null);
    User user2 = new User("이민형", "mh@test.com", "pass1234", null);
    ChannelDto dto = new ChannelDto(UUID.randomUUID(), null, null, ChannelType.PRIVATE, null,
        null);

    given(channelRepository.save(any(Channel.class))).will(returnsFirstArg());
    given(userRepository.findById(uuidList.get(0))).willReturn(Optional.of(user1));
    given(userRepository.findById(uuidList.get(1))).willReturn(Optional.of(user2));
    given(readStatusRepository.save(any(ReadStatus.class))).will(returnsFirstArg());
    given(channelMapper.toDto(any(Channel.class))).willReturn(dto);

    //when
    ChannelDto result = channelService.createPrivateChannel(request);

    //then
    assertThat(result).isNotNull();
    assertThat(result.name()).isNull();

    then(channelRepository).should().save(any(Channel.class));
    then(readStatusRepository).should(times(2))
        .save(any(ReadStatus.class)); // 참여자가 2명이므로, ReadStatus도 정확히 2번(times(2)) 저장되었는지 검증
  }

  @Test
  @DisplayName("채널 정보 수정 성공")
  void update_success() {
    UUID channelId = UUID.randomUUID();
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("공지방", "공지방");
    Channel channel = new Channel("공지방", "공지방", ChannelType.PUBLIC);
    ChannelDto dto = new ChannelDto(channelId, "공지방", "공지방", ChannelType.PUBLIC, null, null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(channelMapper.toDto(channel)).willReturn(dto);

    ChannelDto result = channelService.update(channelId, request);

    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo(request.newName());
    assertThat(channel.getName()).isEqualTo("공지방");
  }

  @Test
  @DisplayName("Private 채널 수정 시 PrivateChannelUpdateDeniedException 발생")
  void update_fail_private_channel_update_denied() {
    UUID channelId = UUID.randomUUID();
    PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("공지방", "공지방");
    Channel channel = new Channel("공지방", "공지방", ChannelType.PRIVATE);
    ChannelDto dto = new ChannelDto(channelId, "공지방", "공지방", ChannelType.PRIVATE, null, null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(PrivateChannelUpdateDeniedException.class);
  }

  @Test
  @DisplayName("특정 사용자의 채널 목록 조회 성공 - 참여 중인 채널이 있는 경우")
  void findAllByUserId_success_with_results() {
    //given
    UUID userId = UUID.randomUUID();
    User user = new User("이마크", "mark@test.com", "pass1234", null);

    Channel privateChannel = new Channel(null, null, ChannelType.PRIVATE);
    Channel publicChannel = new Channel("공지방", "공지방", ChannelType.PUBLIC);
    ReadStatus readStatus = new ReadStatus(user, privateChannel,
        null); // Private 채널은 ReadStatus를 통해 찾으므로 객체 생성

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(readStatusRepository.findAllByUserId(any())).willReturn(List.of(readStatus));

    given(channelRepository.findByTypeOrIdIn(eq(ChannelType.PUBLIC), anyList()))
        .willReturn(List.of(publicChannel, privateChannel));

    ChannelDto dto = new ChannelDto(UUID.randomUUID(), "테스트방", "테스트방", ChannelType.PUBLIC, null,
        null);
    given(channelMapper.toDto(any(Channel.class))).willReturn(
        dto); // any()를 사용하여 어떤 Channel이 들어오든 동일한 가짜 dto를 뱉어내게 한다.

    //when
    List<ChannelDto> result = channelService.findAllByUserId(userId);

    //then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("특정 사용자의 채널 목록 조회 성공 - 참여 중인 채널이 없는 경우")
  void findAllByUserId_success_empty_results() {
    UUID userId = UUID.randomUUID();
    User user = new User("이마크", "mark@test.com", "pass1234", null);
    Channel publicChannel = new Channel("공지방", "공지방", ChannelType.PUBLIC);
    ChannelDto dto = new ChannelDto(UUID.randomUUID(), "테스트방", "테스트방", ChannelType.PUBLIC, null,
        null);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(readStatusRepository.findAllByUserId(any())).willReturn(
        List.of()); //  참여 중인 채널이 없으므로 '빈 리스트'를 반환하도록 명시적으로 모킹. Mockito가 List를 반환하는 메서드를 모킹하지 않으면 기본값으로 '빈 리스트'를 뱉어줌
    given(channelRepository.findByTypeOrIdIn(eq(ChannelType.PUBLIC), anyList())).willReturn(
        List.of(publicChannel)); // 빈 리스트는 Optional.empty()나 null이 아님 주의
    given(channelMapper.toDto(any(Channel.class))).willReturn(dto);

    List<ChannelDto> result = channelService.findAllByUserId(userId);

    assertThat(result.get(0).name()).isEqualTo("테스트방");
    assertThat(result).hasSize(1);
  }

}
