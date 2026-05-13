package com.sprint.mission.discodeit.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.sprint.mission.discodeit.dto.channeldto.ChannelDto;
import com.sprint.mission.discodeit.dto.channeldto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChanelUpdateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.channel.WrongChannelTypeException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.ArrayList;
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
  JPAChannelRepository channelRepository;
  @Mock
  JPAReadStatusRepository readStatusRepository;
  @Mock
  JPAUserRepository userRepository;
  @Mock
  JPAMessageRepository messageRepository;
  @Mock
  ChannelMapper channelMapper;


  @InjectMocks
  BasicChannelService channelService;


  @Test
  @DisplayName("공개 채널 생성 테스트")
  void createPublicChannelTest() {

    //given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(

        "test channelname",
        "channel description"
    );

    given(channelMapper.toDto(any(Channel.class), anyList(), any(Instant.class)))
        .willReturn(new ChannelDto(
            UUID.randomUUID(),
            ChannelType.PUBLIC,
            request.name(),
            request.description(),
            new ArrayList<>(),
            Instant.now()
        ));

    //when
    ChannelDto channelDto = channelService.createPublic(request);

    //then
    assertThat(channelDto).isNotNull();
    assertThat(channelDto.name()).isEqualTo(request.name());
    assertThat(channelDto.description()).isEqualTo(request.description());
    assertThat(channelDto.type()).isEqualTo(ChannelType.PUBLIC);
    then(channelRepository).should().save(any(Channel.class));

  }

  @Test
  @DisplayName("개인 채널 생성 테스트")
  void createPrivateChannelTest() {

    //given
    ArrayList<UUID> userIds = new ArrayList<>();
    userIds.add(UUID.randomUUID());
    userIds.add(UUID.randomUUID());

    User user = new User(
        "test",
        "test@test.com",
        "testpassword",
        null,
        null

    );

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));

    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        userIds
    );

    given(channelMapper.toDto(any(Channel.class), anyList(), any(Instant.class)))
        .willReturn(new ChannelDto(
            UUID.randomUUID(),
            ChannelType.PRIVATE,
            null,
            null,
            new ArrayList<>(),
            Instant.now()
        ));

    //when
    ChannelDto channelDto = channelService.createPrivate(request);

    //then
    assertThat(channelDto).isNotNull();
    assertThat(channelDto.type()).isEqualTo(ChannelType.PRIVATE);
    then(channelRepository).should().save(any(Channel.class));


  }

  @Test
  @DisplayName("채널 업데이트 테스트")
  void channelUpdateTest() {

    //given
    PublicChanelUpdateRequest request = new PublicChanelUpdateRequest(

        "새 업데이트 채널명",
        "새 업데이트 채널 설명"

    );

    given(channelRepository.findById(any(UUID.class))).willReturn(Optional.of(new Channel(
        "기존 채널 이름",
        "기존 채널 설명",
        ChannelType.PUBLIC

    )));

    //when & then
    channelService.updateChannel(UUID.randomUUID(), request);


  }

  @Test
  @DisplayName("개인 채널 업데이트 실패")
  void channelUpdateFailByWrongChannelType() {

    //given
    PublicChanelUpdateRequest request = new PublicChanelUpdateRequest(

        "새 업데이트 채널명",
        "새 업데이트 채널 설명"

    );

    given(channelRepository.findById(any(UUID.class))).willReturn(Optional.of(new Channel(
        "기존 채널 이름",
        "기존 채널 설명",
        ChannelType.PRIVATE

    )));

    //when & then
    assertThatThrownBy(() -> channelService.updateChannel(UUID.randomUUID(), request))
        .isInstanceOf(WrongChannelTypeException.class);


  }

  @Test
  @DisplayName("유저 아이디로 채널 찾기")
  void findChannelByUserId() {

    UUID userId = UUID.randomUUID();

    Channel channel1 = new Channel("채널1", "채널2", ChannelType.PUBLIC);
    Channel channel2 = new Channel("채널2", "채널2", ChannelType.PRIVATE);

    ChannelDto channelDto1 = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "채널1", "채널1",
        null,
        Instant.now());
    ChannelDto channelDto2 = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, "채널2", "채널2",
        null,
        Instant.now());

    doReturn(channelDto1, channelDto2)
        .when(channelMapper)
        .toDto(any(), any(), any());

    given(channelRepository.findAllByUser_Id(userId))
        .willReturn(List.of(channel1, channel2));
    //when
    List<ChannelDto> channelList = channelService.findAllByUserId(userId);

    //then
    assertThat(channelList).hasSize(2);
    assertThat(channelList.get(0).name()).isEqualTo("채널1");
    assertThat(channelList.get(1).name()).isEqualTo("채널2");

  }

  @Test
  @DisplayName("유저 아이디로 채널 찾기 실패")
  void findChannelByUserIdFailByNonExistUser() {

    UUID userId = UUID.randomUUID();

    Channel channel1 = new Channel("채널1", "채널2", ChannelType.PUBLIC);
    Channel channel2 = new Channel("채널2", "채널2", ChannelType.PRIVATE);

    ChannelDto channelDto1 = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "채널1", "채널1",
        null,
        Instant.now());
    ChannelDto channelDto2 = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, "채널2", "채널2",
        null,
        Instant.now());

    given(channelRepository.findAllByUser_Id(userId))
        .willReturn(new ArrayList<>());
    //when
    List<ChannelDto> channelList = channelService.findAllByUserId(userId);

    //then
    assertThat(channelList).hasSize(0);


  }


}
