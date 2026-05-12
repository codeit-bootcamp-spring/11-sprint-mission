package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

    @Mock private ChannelRepository channelRepository;
    @Mock private ReadStatusRepository readStatusRepository;
    @Mock private UserRepository userRepository;
    @Mock private MessageRepository messageRepository; // 현재 쓰이지 않지만 주입을 위해 필요
    @Mock private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService channelService;

    @Captor
    private ArgumentCaptor<Channel> channelCaptor; // 생성된 채널을 가로채기 위함

    // [테스트 목표 2] 제네릭 컬렉션(List) 타입 캡처를 위한 전용 캡처 객체
    @Captor
    private ArgumentCaptor<List<ReadStatus>> readStatusListCaptor;

    @Nested
    @DisplayName("create(PrivateChannelCreateRequest) 메서드는")
    class Describe_create_private {

        @Nested
        @DisplayName("존재하는 참가자 ID 목록이 주어지면")
        class Context_with_valid_participant_ids {
            @Test
            @DisplayName("프라이빗 채널을 생성하고, 참가자 수만큼 ReadStatus를 저장한다.")
            void it_saves_channel_ans_read_status() {
                // given
                UUID user1Id = UUID.randomUUID();
                UUID user2Id = UUID.randomUUID();
                List<UUID> participantIds = List.of(user1Id, user2Id);

                PrivateChannelCreateRequest privateChannelCreateRequest = new PrivateChannelCreateRequest(participantIds);

                User user1 = new User("user1", "user1@email.com", "password123", null);
                User user2 = new User("user2", "user2@email.com", "password123", null);
                List<User> foundUsers = List.of(user1, user2);

                given(userRepository.findAllById(privateChannelCreateRequest.participantIds())).willReturn(foundUsers);

                ChannelDto expectedDto = new ChannelDto(
                        UUID.randomUUID(), ChannelType.PRIVATE, null, null, List.of(), Instant.now());
                given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

                // when
                ChannelDto result = channelService.create(privateChannelCreateRequest);

                // then
                assertThat(result).isNotNull();

                verify(channelRepository, times(1)).save(channelCaptor.capture());
                Channel savedChannel = channelCaptor.getValue();
                assertThat(savedChannel.getType()).isEqualTo(ChannelType.PRIVATE);

                verify(readStatusRepository).saveAll(readStatusListCaptor.capture());
                List<ReadStatus> savedReadStatuses = readStatusListCaptor.getValue();
                assertThat(savedReadStatuses).hasSize(2);
                // ReadStatus의 user들이 올바르게 들어가 있는지
                assertThat(savedReadStatuses)
                        .extracting("user")
                        .containsExactlyInAnyOrder(user1, user2);

                // ReadStatus의 channel이 모두 다 같은지
                assertThat(savedReadStatuses)
                        .extracting("channel")
                        .containsOnly(savedChannel);
            }
        }
    }

    @Captor
    ArgumentCaptor<List<UUID>> uuidListCaptor;

    @Nested
    @DisplayName("findAllByUserId() 메서드는")
    class Describe_findAllByUserId {

        UUID userId = UUID.randomUUID();

        @Nested
        @DisplayName("사용자가 구독 중인 프라이빗 채널이 있는 경우")
        class Context_with_subscribed_channels {
            @Test
            @DisplayName("구독 중인 채널 ID를 추출하여 PUBLIC 채널과 함께 통합 조회한다.")
            void it_fetches_public_and_subscribed_private_channels() {
                // given
                // 프라이빗 채널 2개 생성 및 강제fh ID를 부여
                Channel privateChannel1 = new Channel(ChannelType.PRIVATE, null, null);
                ReflectionTestUtils.setField(privateChannel1, "id", UUID.randomUUID());
                Channel privateChannel2 = new Channel(ChannelType.PRIVATE, null, null);
                ReflectionTestUtils.setField(privateChannel2, "id", UUID.randomUUID());

                // 해당 채널에 대한 ReadStatus 생성
                User mockUser = new User("user", "user@email.com", "password123", null);
                ReadStatus status1 = new ReadStatus(mockUser, privateChannel1, Instant.now());
                ReadStatus status2 = new ReadStatus(mockUser, privateChannel2, Instant.now());

                given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of(status1, status2));

                Channel publicChannel = new Channel(ChannelType.PUBLIC, "공개방", "설명");
                ReflectionTestUtils.setField(publicChannel, "id", UUID.randomUUID());

                List<Channel> combinedChannels = List.of(publicChannel, privateChannel1, privateChannel2);

                List<UUID> expectedExtractedIds = List.of(privateChannel1.getId(), privateChannel2.getId());
                given(channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), eq(expectedExtractedIds)))
                        .willReturn(combinedChannels);

                given(channelMapper.toDto(any(Channel.class))).willAnswer(invocation -> {
                    Channel channel = invocation.getArgument(0);
                    return new ChannelDto(
                            channel.getId(),
                            channel.getType(),
                            channel.getName(),
                            channel.getDescription(),
                            List.of(), // 그냥 빈 리스트로 (테스트니깐)
                            Instant.now()
                    );
                });

                // when
                List<ChannelDto> result = channelService.findAllByUserId(userId);

                // then
                assertThat(result).hasSize(3);

                verify(channelRepository).findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), uuidListCaptor.capture());
                List<UUID> capturedIds = uuidListCaptor.getValue();

                assertThat(capturedIds).containsExactly(privateChannel1.getId(), privateChannel2.getId());

                // Private Channel 2개 / Public Channel 1개
                assertThat(result)
                        .extracting("type")
                        .containsExactlyInAnyOrder(ChannelType.PUBLIC, ChannelType.PRIVATE, ChannelType.PRIVATE);
            }
        }

        @Nested
        @DisplayName("사용자가 아무 채널도 구독하고 있지 않은 경우")
        class Context_with_no_subscribed_channels {
            @Test
            @DisplayName("빈 리스트를 파라미터로 넘기고, PUBLIC 채널만 조회하여 반환한다.")
            void it_fetches_only_public_channels() {
                // given
                // 구독 정보가 하나도 없음 (빈 리스트)
                given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());

                Channel publicChannel = new Channel(
                        ChannelType.PUBLIC, "공개방", "desc");
                given(channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), eq(List.of())))
                        .willReturn(List.of(publicChannel));

                given(channelMapper.toDto(any(Channel.class))).willReturn(
                        new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "공개방", "desc", List.of(), Instant.now())
                );

                // when
                List<ChannelDto> result = channelService.findAllByUserId(userId);

                // then
                assertThat(result).hasSize(1);

                // 빈 리스트가 IN 쿼리 파라미터로 잘 전달 되었는지 확인
                verify(channelRepository).findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), eq(List.of()));
            }
        }
    }

    @Nested
    @DisplayName("delete() 메서드는")
    class Describe_delete {

        UUID channelId = UUID.randomUUID();

        @Nested
        @DisplayName("존재하지 않는 채널 ID가 주어지면")
        class Context_with_not_found_channel_id {
            @Test
            @DisplayName("ChannelNotFoundException 예외를 던지고, 어떤 삭제 로직도 실행하지 않는다.")
            void it_throws_ChannelNotFoundException() {
                // given
                given(channelRepository.existsById(channelId)).willReturn(false);

                // when & then
                assertThatThrownBy(() -> channelService.delete(channelId))
                        .isInstanceOf(ChannelNotFoundException.class);

                // 삭제 로직들을 절대 호출하면 안됨!!!
                verify(messageRepository, never()).deleteAllByChannelId(any());
                verify(readStatusRepository, never()).deleteAllByChannelId(any());
                verify(channelRepository, never()).deleteById(any());
            }
        }

        @Nested
        @DisplayName("존재하는 채널 ID가 주어지면")
        class Context_with_valid_channel_id {
            @Test
            @DisplayName("연관된 메지지와 읽음 상태를 모두 삭제한 후 최종적으로 채널을 삭제한다.")
            void it_deletes_channel_and_associated_data() {
                // given
                given(channelRepository.existsById(channelId)).willReturn(true);

                // when
                channelService.delete(channelId);

                // then
                // - 딱 한 번 씩만 호출되었는지도 중요함!!
                verify(messageRepository, times(1)).deleteAllByChannelId(channelId);
                verify(readStatusRepository, times(1)).deleteAllByChannelId(channelId);
                verify(channelRepository, times(1)).deleteById(channelId);
            }

            @Test
            @DisplayName("삭제 로직이 의도된 순서대로 정확히 실행되는지 검증한다.")
            void it_deletes_in_specific_order() {
                // given
                given(channelRepository.existsById(channelId)).willReturn(true);

                // when
                channelService.delete(channelId);

                // then
                // InOrder를 사용하면 메서드 호출 횟수 뿐만 아니라 순서까지 검증할 수 있음
                // - 여기는 순서랑 상관없음 (참여자를 넣는 것뿐)
                InOrder inOrder = inOrder(messageRepository, readStatusRepository, channelRepository);

                // - 여기 순서는 매우 중요!!
                inOrder.verify(messageRepository, times(1)).deleteAllByChannelId(channelId);
                inOrder.verify(readStatusRepository, times(1)).deleteAllByChannelId(channelId);
                inOrder.verify(channelRepository, times(1)).deleteById(channelId);
            }
        }
    }
}

// given(목객체.메서드()).willAnswer() - 정방향 기본형
// - **반환값이 있는 메서드에만 사용할 수 있음**
// - 자바 문법상 given()이라는 메서드의 괄호 안에는 값이 들어가야함

// willAnswer().given(목객체).메서드() - 도치법 형태
// - 반환값이 없는 void 메서드를 모킹할 때 반드시 사용해야함
//      - 반환값이 없어도 사용할 수 있음
// - 조건 모킹하려는 메서드가 void일 때
// - void 메서드는 뱉어내는 값이 아예 없으므로, 자바 컴파일러는 given(목객체.delete())라는 코드를 보면 에러는 뱉음
// ```willAnswer(invocation -> {
//    System.out.println("삭제 로직 가로챔!");
//    return null;
//}).given(userRepository).delete(any(User.class));
// ```

// verify는 기본적으로 times(1)
// - 그래도 명시적으로 작성하는 것이 좋아보인다

// 주의 - inOrder
// - inOrder.verify()를 할 때 호출되는 순서를 명시적으로 맞춰줘야함