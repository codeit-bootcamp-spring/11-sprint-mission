package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

    @Mock
    MessageRepository messageRepository;
    @Mock
    MessageMapper messageMapper;
    @Mock
    PageResponseMapper pageResponseMapper;

    @InjectMocks
    BasicMessageService messageService;

    @Captor
    ArgumentCaptor<Instant> timeCaptor;

    @Nested
    @DisplayName("findAllByChannelId() 메서드는")
    class Describe_findAllByChannelId {

        UUID channelId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20); // 0페이지, 20개 가져오기

        @Nested
        @DisplayName("조회된 메시지 데이터가 존재할 때")
        class Context_with_messages {

            @Test
            @DisplayName("마지막 메세지의 createdAt을 다음 커서로 추출하여 반환한다.")
            void it_extracts_next_cursor_from_last_message() {
                // given
                Instant cursorTime = Instant.parse("2026-05-07T10:00:00Z");

                Message msg1 = new Message("첫번째", new Channel(ChannelType.PUBLIC, null, null), new User("a", "a@a.com", "p", null), List.of());
                Message msg2 = new Message("두번째", new Channel(ChannelType.PUBLIC, null, null), new User("b", "b@b.com", "p", null), List.of());

                Slice<Message> mockSlice = new SliceImpl<>(List.of(msg1, msg2), pageable, true);
                given(messageRepository.findAllByChannelIdWithAuthor(channelId, cursorTime, pageable))
                        .willReturn(mockSlice);

                Instant time1 = Instant.parse("2026-05-07T09:50:00Z");
                Instant time2 = Instant.parse("2026-05-07T09:40:00Z"); // 가장 오래된(마지막) 데이터의 시간
                MessageDto dto1 = new MessageDto(UUID.randomUUID(), time1, time1, "첫번째", null, null, List.of());
                MessageDto dto2 = new MessageDto(UUID.randomUUID(), time2, time2, "두번째", null, null, List.of());

                given(messageMapper.toDto(msg1)).willReturn(dto1);
                given(messageMapper.toDto(msg2)).willReturn(dto2);

                // 최종 반환할 응답 객체 모킹
                PageResponse<MessageDto> expectedResponse = new PageResponse<>(
                        List.of(dto1, dto2),
                        time2,               // nextCursor: 다음 조회를 위한 커서 (마지막 데이터의 시간)
                        2,                   // size: 현재 응답의 데이터 개수
                        true,                // hasNext: 다음 페이지가 있는지 여부
                        null                 // totalElements: 전체 데이터 개수 (Slice에서는 알 수 없으므로 null)
                );
                // pageResponseMapper에 time2가 정확히 다음 커서로 들어가는지 조건 설정
                given(pageResponseMapper.fromSlice(any(Slice.class), eq(time2))).willReturn(expectedResponse);

                // when
                PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, cursorTime, pageable);

                // then
                assertThat(result).isEqualTo(expectedResponse);

                // 서비스 로직에서 추출한 nextCursor(time2)가 Mapper에 정확히 인자로 넘어갔는지 행위 검증
                verify(pageResponseMapper).fromSlice(any(Slice.class), eq(time2));
            }
        }

        @Nested
        @DisplayName("createAt 파라미터가 null로 들어오면 (최초 조회)")
        class Context_with_null_createAt {
            @Test
            @DisplayName("현재 시간(Instant.now())를 기본값으로 사용하여 조회한다.")
            void it_uses_current_time_as_fallback() {
                // given
                // 빈 슬라이스 반환
                Slice<Message> emptySlice = new SliceImpl<>(List.of(), pageable, false);

                // createAt 자리에 timeCaptor.capture()를 배치하여 들어오는 시간 값을 낚아챔
                given(messageRepository.findAllByChannelIdWithAuthor(eq(channelId), timeCaptor.capture(), eq(pageable)))
                        .willReturn(emptySlice);

                // when
                messageService.findAllByChannelId(channelId, null, pageable);

                // then
                Instant capturedTime = timeCaptor.getValue();

                // 캡처된 시간이 '현재 시간'과 거의 일치하는지 (1초 오차 허용) 검증
                assertThat(capturedTime)
                        .isNotNull()
                        .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
            }
        }

        @Nested
        @DisplayName("조회된 메시지가 하나도 없을 때 (마지막 페이지)")
        class Context_with_empty_messages {

            @Test
            @DisplayName("nextCursor를 null로 전달한다.")
            void it_passes_null_cursor() {
                // given
                Instant cursorTime = Instant.now();
                Slice<Message> emptySlice = new SliceImpl<>(List.of(), pageable, false);

                given(messageRepository.findAllByChannelIdWithAuthor(channelId, cursorTime, pageable))
                        .willReturn(emptySlice);

                // when
                messageService.findAllByChannelId(channelId, cursorTime, pageable);

                // then
                // 리스트가 비어있으므로 nextCursor 값에 null이 들어갔는지 검증
                verify(pageResponseMapper).fromSlice(any(Slice.class), isNull());
            }
        }
    }
}

// Mock / InjectMock
// - 만약 사용하지 않을거를 Mock하지 않으면?
// - InjectMock에서 null이 들어가게 됨
// - 테스트를 할 때 사용하는 거만 불러와도 됨
// - Pageable / Slice는 조금 더 공부