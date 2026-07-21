package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static com.sprint.mission.discodeit.entity.BinaryContent.BinaryContentStatus.PROCESSING;

@ExtendWith(MockitoExtension.class)
class BasicBinaryContentServiceTest {

    @Mock
    BinaryContentRepository binaryContentRepository;

    @Mock
    BinaryContentMapper binaryContentMapper;

    @Mock
    BinaryContentStorage binaryContentStorage;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    BasicBinaryContentService binaryContentService;

    @Test
    void create_success_withBytes() {
        // given
        byte[] bytes = "hello".getBytes();

        BinaryContentCreateRequest request = new BinaryContentCreateRequest(
                "hello.txt",
                "text/plain",
                bytes
        );

        BinaryContent saved = new BinaryContent(
                request.fileName(),
                bytes.length,
                request.contentType()
        );

        BinaryContentDto expectedDto = new BinaryContentDto(
                saved.getId(),
                "hello.txt",
                (long) bytes.length,
                "text/plain",
                PROCESSING
        );

        given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(saved);
        given(binaryContentMapper.toDto(saved)).willReturn(expectedDto);

        // when
        BinaryContentDto result = binaryContentService.create(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(binaryContentRepository).should().save(any(BinaryContent.class));
        then(eventPublisher).should().publishEvent(any(BinaryContentCreatedEvent.class));
        then(binaryContentStorage).should(never()).put(any(UUID.class), any(byte[].class));
        then(binaryContentMapper).should().toDto(saved);
    }

    @Test
    void create_success_withoutBytes() {
        // given
        BinaryContentCreateRequest request = new BinaryContentCreateRequest(
                "empty.txt",
                "text/plain",
                null
        );

        BinaryContent saved = new BinaryContent(
                request.fileName(),
                0L,
                request.contentType()
        );

        BinaryContentDto expectedDto = new BinaryContentDto(
                saved.getId(),
                "empty.txt",
                0L,
                "text/plain",
                PROCESSING
        );

        given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(saved);
        given(binaryContentMapper.toDto(saved)).willReturn(expectedDto);

        // when
        BinaryContentDto result = binaryContentService.create(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(binaryContentRepository).should().save(any(BinaryContent.class));
        then(binaryContentStorage).should(never()).put(any(UUID.class), any(byte[].class));
        then(binaryContentMapper).should().toDto(saved);
    }

    @Test
    void find_success() {
        // given
        BinaryContent binaryContent = new BinaryContent(
                "image.png",
                100L,
                "image/png"
        );

        BinaryContentDto expectedDto = new BinaryContentDto(
                binaryContent.getId(),
                "image.png",
                100L,
                "image/png",
                PROCESSING
        );

        given(binaryContentRepository.findById(binaryContent.getId()))
                .willReturn(Optional.of(binaryContent));
        given(binaryContentMapper.toDto(binaryContent)).willReturn(expectedDto);

        // when
        Optional<BinaryContentDto> result = binaryContentService.find(binaryContent.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDto);
    }

    @Test
    void find_empty_whenNotFound() {
        // given
        UUID id = UUID.randomUUID();

        given(binaryContentRepository.findById(id)).willReturn(Optional.empty());

        // when
        Optional<BinaryContentDto> result = binaryContentService.find(id);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findAllByIdIn_success() {
        // given
        BinaryContent binaryContent = new BinaryContent(
                "image.png",
                100L,
                "image/png"
        );

        BinaryContentDto dto = new BinaryContentDto(
                binaryContent.getId(),
                "image.png",
                100L,
                "image/png",
                PROCESSING
        );

        given(binaryContentRepository.findAllById(List.of(binaryContent.getId())))
                .willReturn(List.of(binaryContent));
        given(binaryContentMapper.toDto(binaryContent)).willReturn(dto);

        // when
        List<BinaryContentDto> result = binaryContentService.findAllByIdIn(List.of(binaryContent.getId()));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    void delete_success() {
        // given
        BinaryContent binaryContent = new BinaryContent("image.png", 100L, "image/png");

        given(binaryContentRepository.findById(binaryContent.getId()))
                .willReturn(Optional.of(binaryContent));

        // when
        binaryContentService.delete(binaryContent.getId());

        // then
        then(binaryContentRepository).should().findById(binaryContent.getId());
        then(binaryContentRepository).should().deleteById(binaryContent.getId());
    }

    @Test
    void delete_fail_whenNotFound() {
        // given
        UUID id = UUID.randomUUID();

        given(binaryContentRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> binaryContentService.delete(id))
                .isInstanceOf(RuntimeException.class);

        then(binaryContentRepository).should().findById(id);
        then(binaryContentRepository).should(never()).deleteById(id);
    }

    @Test
    void findEntity_success() {
        // given
        BinaryContent binaryContent = new BinaryContent("image.png", 100L, "image/png");

        given(binaryContentRepository.findById(binaryContent.getId()))
                .willReturn(Optional.of(binaryContent));

        // when
        Optional<BinaryContent> result = binaryContentService.findEntity(binaryContent.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(binaryContent);
    }
}
