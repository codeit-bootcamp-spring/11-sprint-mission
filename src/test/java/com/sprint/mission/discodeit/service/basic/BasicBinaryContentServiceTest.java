package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.FileSaveFailedException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicBinaryContentServiceTest {

    @Mock
    private BinaryContentRepository binaryContentRepo;

    @Mock
    private BinaryContentMapper binaryContentMapper;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private BasicBinaryContentService binaryContentService;

    @Test
    void create_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "image".getBytes()
        );

        BinaryContent result = binaryContentService.create(file);

        assertThat(result.getFileName()).isEqualTo("image.png");
        assertThat(result.getContentType()).isEqualTo("image/png");
        assertThat(result.getSize()).isEqualTo(5L);

        then(binaryContentRepo).should().save(any(BinaryContent.class));
        then(binaryContentStorage).should().put(any(UUID.class), org.mockito.ArgumentMatchers.eq("image".getBytes()));
    }

    @Test
    void create_success_whenFileNull() {
        BinaryContent result = binaryContentService.create(null);

        assertThat(result).isNull();

        then(binaryContentRepo).should(never()).save(any());
        then(binaryContentStorage).should(never()).put(any(), any());
    }

    @Test
    void create_fail_whenIOException() throws IOException {
        given(multipartFile.isEmpty()).willReturn(false);
        given(multipartFile.getOriginalFilename()).willReturn("image.png");
        given(multipartFile.getBytes()).willThrow(new IOException());

        assertThatThrownBy(() -> binaryContentService.create(multipartFile))
                .isInstanceOf(FileSaveFailedException.class);

        then(binaryContentRepo).should(never()).save(any());
        then(binaryContentStorage).should(never()).put(any(), any());
    }

    @Test
    void createAll_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "image".getBytes()
        );

        List<BinaryContent> result = binaryContentService.createAll(List.of(file));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("image.png");

        then(binaryContentRepo).should().save(any(BinaryContent.class));
        then(binaryContentStorage).should().put(any(UUID.class), org.mockito.ArgumentMatchers.eq("image".getBytes()));
    }

    @Test
    void createAll_success_whenFilesEmpty() {
        List<BinaryContent> result = binaryContentService.createAll(List.of());

        assertThat(result).isEmpty();

        then(binaryContentRepo).should(never()).save(any());
    }

    @Test
    void find_success() {
        UUID binaryContentId = UUID.randomUUID();
        BinaryContent binaryContent = new BinaryContent("image.png", "image/png", 1000L);
        BinaryContentDto expected = new BinaryContentDto(binaryContentId, "image.png", 1000L, "image/png");

        given(binaryContentRepo.findById(binaryContentId)).willReturn(Optional.of(binaryContent));
        given(binaryContentMapper.toDto(binaryContent)).willReturn(expected);

        BinaryContentDto result = binaryContentService.find(binaryContentId);

        assertThat(result).isEqualTo(expected);

        then(binaryContentRepo).should().findById(binaryContentId);
        then(binaryContentMapper).should().toDto(binaryContent);
    }

    @Test
    void find_fail_whenBinaryContentNotFound() {
        UUID binaryContentId = UUID.randomUUID();

        given(binaryContentRepo.findById(binaryContentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> binaryContentService.find(binaryContentId))
                .isInstanceOf(BinaryContentNotFoundException.class);

        then(binaryContentMapper).should(never()).toDto(any());
    }

    @Test
    void findAllByIdIn_success() {
        UUID id = UUID.randomUUID();
        BinaryContent binaryContent = new BinaryContent("image.png", "image/png", 1000L);
        BinaryContentDto dto = new BinaryContentDto(id, "image.png", 1000L, "image/png");

        given(binaryContentRepo.findAllById(org.mockito.ArgumentMatchers.anySet()))
                .willReturn(List.of(binaryContent));
        given(binaryContentMapper.toDto(binaryContent)).willReturn(dto);

        List<BinaryContentDto> result = binaryContentService.findAllByIdIn(List.of(id));

        assertThat(result).containsExactly(dto);
    }

    @Test
    void findAllByIdIn_fail_whenMissingIdExists() {
        UUID id = UUID.randomUUID();

        given(binaryContentRepo.findAllById(org.mockito.ArgumentMatchers.anySet()))
                .willReturn(List.of());

        assertThatThrownBy(() -> binaryContentService.findAllByIdIn(List.of(id)))
                .isInstanceOf(BinaryContentNotFoundException.class);

        then(binaryContentMapper).should(never()).toDto(any());
    }

    @Test
    void delete_success() {
        BinaryContent binaryContent = new BinaryContent("image.png", "image/png", 1000L);

        binaryContentService.delete(binaryContent);

        then(binaryContentStorage).should().deleteById(binaryContent.getId());
        then(binaryContentRepo).should().delete(binaryContent);
    }

    @Test
    void delete_success_whenBinaryContentNull() {
        binaryContentService.delete(null);

        then(binaryContentStorage).should(never()).deleteById(any());
        then(binaryContentRepo).should(never()).delete(any());
    }

    @Test
    void deleteAll_success() {
        BinaryContent binaryContent = new BinaryContent("image.png", "image/png", 1000L);

        binaryContentService.deleteAll(List.of(binaryContent));

        then(binaryContentStorage).should().deleteById(binaryContent.getId());
        then(binaryContentRepo).should().deleteAll(List.of(binaryContent));
    }

    @Test
    void deleteAll_success_whenListEmpty() {
        binaryContentService.deleteAll(List.of());

        then(binaryContentStorage).should(never()).deleteById(any());
        then(binaryContentRepo).should(never()).deleteAll(any());
    }
}
