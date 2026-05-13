package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BinaryContentServiceTest {

    @Mock private BinaryContentRepository binaryContentRepository;
    @Mock private BinaryContentMapper binaryContentMapper;
    @Mock private BinaryContentStorage binaryContentStorage;

    @InjectMocks private BinaryContentService binaryContentService;

    private BinaryContent binaryContent;
    private BinaryContentDto binaryContentDto;

    @BeforeEach
    void setUp() {
        binaryContent = new BinaryContent(new byte[]{1, 2, 3}, "test.png", "image/png");
        binaryContentDto = BinaryContentDto.builder()
                .id(binaryContent.getId())
                .fileName("test.png")
                .size(3L)
                .contentType("image/png")
                .build();
    }

    @Test
    void find_성공() {
        UUID id = binaryContent.getId();
        given(binaryContentRepository.findById(id)).willReturn(Optional.of(binaryContent));
        given(binaryContentMapper.toDto(binaryContent)).willReturn(binaryContentDto);

        BinaryContentDto result = binaryContentService.find(id);

        assertThat(result).isEqualTo(binaryContentDto);
    }

    @Test
    void find_없음_예외() {
        UUID id = UUID.randomUUID();
        given(binaryContentRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> binaryContentService.find(id))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void find_id_null_예외() {
        assertThatThrownBy(() -> binaryContentService.find(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void findAllByIdIn_성공() {
        List<UUID> ids = List.of(binaryContent.getId());
        given(binaryContentRepository.findAllByIdIn(ids)).willReturn(List.of(binaryContent));
        given(binaryContentMapper.toDto(binaryContent)).willReturn(binaryContentDto);

        List<BinaryContentDto> result = binaryContentService.findAllByIdIn(ids);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(binaryContentDto);
    }

    @Test
    void findAllByIdIn_빈_목록_반환() {
        List<BinaryContentDto> result = binaryContentService.findAllByIdIn(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByIdIn_null_빈_목록_반환() {
        List<BinaryContentDto> result = binaryContentService.findAllByIdIn(null);

        assertThat(result).isEmpty();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void download_성공() {
        UUID id = binaryContent.getId();
        ResponseEntity expected = ResponseEntity.ok().build();

        given(binaryContentRepository.findById(id)).willReturn(Optional.of(binaryContent));
        given(binaryContentMapper.toDto(binaryContent)).willReturn(binaryContentDto);
        given(binaryContentStorage.download(binaryContentDto)).willReturn(expected);

        ResponseEntity<?> result = binaryContentService.download(id);

        assertThat(result).isEqualTo(expected);
    }
}
