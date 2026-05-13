package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
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
  private ChannelMapper channelMapper;

  @InjectMocks
  private BasicChannelService channelService;

  // Create 테스트
  @Test
  @DisplayName("퍼블릭 채널 생성 성공")
  void createPublicChannel_success() {
    // Given
    ChannelDto.CreatePublicRequest request = new ChannelDto.CreatePublicRequest("공개 채",
        "공개 채널입니다.");
    ChannelDto.Response mockResponse = ChannelDto.Response.builder()
        .name(request.name())
        .description(request.description())
        .type(request.toEntity().getType()).build();

    given(channelMapper.toDto(any(Channel.class))).willReturn(mockResponse);

    // When
    ChannelDto.Response result = channelService.createPublicChannel(request);

    // Then
    assertThat(result.name()).isEqualTo(request.name());
    assertThat(result.type()).isEqualTo(request.toEntity().getType());

    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("프라이빗 채널 생성 성공")
  void createPrivateChannel_success() {
    // Given
    UUID participantId = UUID.randomUUID();
    ChannelDto.CreatePrivateRequest request = new ChannelDto.CreatePrivateRequest(
        List.of(participantId));
    User mockUser = User.builder().username("woody").build();
    ChannelDto.Response mockResponse = ChannelDto.Response.builder()
        .type(request.toEntity().getType())
        .build();

    given(userRepository.findAllById(request.participantIds())).willReturn(List.of(mockUser));
    given(channelMapper.toDto(any(Channel.class))).willReturn(mockResponse);

    // When
    ChannelDto.Response result = channelService.createPrivateChannel(request);

    // Then
    assertThat(result.type()).isEqualTo(request.toEntity().getType());
    then(channelRepository).should().save(any(Channel.class));
    then(readStatusRepository).should().saveAll(anyList());
  }

  // Update 테스트
  @Test
  @DisplayName("퍼블릭 채널 수정 성공")
  void update_success() {
    // Given
    UUID channelId = UUID.randomUUID();
    Channel existingChannel = Channel.createPublic("oldName", "oldDescription");
    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest("newName",
        "newDescription");
    ChannelDto.Response mockResponse = ChannelDto.Response.builder()
        .name(request.newName())
        .build();

    given(channelRepository.findById(channelId)).willReturn(Optional.of(existingChannel));
    given(channelMapper.toDto(any(Channel.class))).willReturn(mockResponse);

    // When
    ChannelDto.Response result = channelService.update(channelId, request);

    // Then
    assertThat(result.name()).isEqualTo(request.newName());
    assertThat(existingChannel.getName()).isEqualTo(request.newName());
    assertThat(existingChannel.getDescription()).isEqualTo(request.newDescription());
  }

  @Test
  @DisplayName("프라이빗 채널 수정 시도 시 PrivateChannelUpdateException 발생")
  void update_fail_privateChannel() {
    // Given
    UUID channelId = UUID.randomUUID();
    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest("newName", "newDescription");
    Channel privateChannel = Channel.createPrivate();

    given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

    // When & Then
    assertThatThrownBy(() -> channelService.update(channelId, request))
        .isInstanceOf(PrivateChannelUpdateException.class);
  }

  @Test
  @DisplayName("존재하지 않는 채널 수정 시 ChannelNotFoundException 발생")
  void update_fail_channelNotFound() {
    // Given
    UUID notExistingId = UUID.randomUUID();
    ChannelDto.UpdateRequest request = new ChannelDto.UpdateRequest("newName", "newDescription");

    given(channelRepository.findById(notExistingId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> channelService.update(notExistingId, request))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  // Delete 테스트
  @Test
  @DisplayName("채널 삭제 성공")
  void delete_success() {
    // Given
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(true);

    // When
    channelService.delete(channelId);

    // Then
    then(messageRepository).should().deleteByChannelId(channelId); // 메시지 먼저 삭제되는지 검증
    then(readStatusRepository).should().deleteByChannelId(channelId); // 읽음 상태 삭제되는지 검증
    then(channelRepository).should().deleteById(channelId); // 채널 삭제되는지 검증
  }

  @Test
  @DisplayName("존재하지 않는 채널 삭제 시 ChannelNotFoundException 발생")
  void delete_fail_channelNotFound() {
    // Given
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(false);

    // When & Then
    assertThatThrownBy(() -> channelService.delete(channelId))
        .isInstanceOf(ChannelNotFoundException.class);

    // 하위 삭제 검증
    then(messageRepository).shouldHaveNoInteractions();
    then(channelRepository).shouldHaveNoMoreInteractions();
  }

  // FindById 테스트
  @Test
  @DisplayName("채널 단건 조회 성공")
  void findById_success() {
    // Given
    UUID channelId = UUID.randomUUID();
    Channel mockChannel = Channel.createPublic("공개 채널", "설명");
    ChannelDto.Response mockResponse = ChannelDto.Response.builder()
        .name("공개 채널")
        .build();

    given(channelRepository.findById(channelId)).willReturn(Optional.of(mockChannel));
    given(channelMapper.toDto(mockChannel)).willReturn(mockResponse);

    // When
    ChannelDto.Response result = channelService.findById(channelId);

    // Then
    assertThat(result.name()).isEqualTo("공개 채널");

    then(channelRepository).should().findById(channelId);
    then(channelMapper).should().toDto(mockChannel);
  }

  @Test
  @DisplayName("존재하지 않는 채널 단건 조회 시 ChannelNotFoundException 발생")
  void findById_fail_channelNotFound() {
    // Given
    UUID notExistingId = UUID.randomUUID();

    given(channelRepository.findById(notExistingId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> channelService.findById(notExistingId))
        .isInstanceOf(ChannelNotFoundException.class);

    then(channelRepository).should().findById(notExistingId);
    then(channelMapper).shouldHaveNoInteractions();
  }

  // findAllByUserId 테스트
  @Test
  @DisplayName("사용자 소속 채널 목록 조회 성공")
  void findAllByUserId_withResults() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    Channel mockChannel = Channel.createPublic("공개 채널", "공개 채널입니다.");
    ReflectionTestUtils.setField(mockChannel, "id", channelId);

    ReadStatus mockReadStatus = ReadStatus.builder().channel(mockChannel).build();
    ChannelDto.Response mockResponse = ChannelDto.Response.builder().name("널 채널").build();

    given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of(mockReadStatus));
    given(
        channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC),
            eq(List.of(mockChannel.getId()))))
        .willReturn(List.of(mockChannel));
    given(channelMapper.toDto(any(Channel.class))).willReturn(mockResponse);

    // When
    List<ChannelDto.Response> results = channelService.findAllByUserId(userId);

    // Then
    assertThat(results).containsExactly(mockResponse);
  }

  @Test
  @DisplayName("사용자 소속 채널 목록 조회 성공 - 소속 채널이 없는 경우")
  void findAllByUserId_empty() {
    // Given
    UUID userId = UUID.randomUUID();

    given(readStatusRepository.findAllByUserId(userId)).willReturn(Collections.emptyList());
    given(
        channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), eq(Collections.emptyList())))
        .willReturn(Collections.emptyList());

    // When
    List<ChannelDto.Response> results = channelService.findAllByUserId(userId);

    // Then
    assertThat(results).isEmpty();
  }
}