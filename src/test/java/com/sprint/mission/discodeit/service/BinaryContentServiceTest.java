package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.CreateBinaryContentRequestDTO;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "app.data.binarycontent-path=./build/test-data/binarycontent.dat"
})
class BinaryContentServiceTest {

    @Autowired private BinaryContentService binaryContentService;

    @Test
    @DisplayName("BinaryContent를 정상적으로 생성하고 조회할 수 있다.")
    void createAndFindTest() {
        // given
        byte[] testData = "test image data bytes".getBytes();
        CreateBinaryContentRequestDTO req = new CreateBinaryContentRequestDTO("profile.png", "image/png", testData);

        // when
        BinaryContent savedContent = binaryContentService.create(req);
        BinaryContent foundContent = binaryContentService.find(savedContent.getId());

        // then
        assertThat(foundContent.getId()).isEqualTo(savedContent.getId());
        assertThat(foundContent.getFileName()).isEqualTo("profile.png");
        assertThat(foundContent.getContentType()).isEqualTo("image/png");
        assertThat(foundContent.getData()).isEqualTo(testData);
    }

    @Test
    @DisplayName("여러 개의 첨부 파일(BinaryContent) ID 목록으로 한 번에 조회할 수 있다.")
    void findAllByIdInTest() {
        // given
        byte[] data1 = "data1".getBytes();
        byte[] data2 = "data2".getBytes();
        BinaryContent saved1 = binaryContentService.create(new CreateBinaryContentRequestDTO("file1.png", "image/png", data1));
        BinaryContent saved2 = binaryContentService.create(new CreateBinaryContentRequestDTO("file2.png", "image/png", data2));

        // when
        List<BinaryContent> foundList = binaryContentService.findAllByIdIn(List.of(saved1.getId(), saved2.getId()));

        // then
        assertThat(foundList).hasSize(2);
        assertThat(foundList.stream().map(BinaryContent::getFileName))
                .containsExactlyInAnyOrder("file1.png", "file2.png");
    }

    @Test
    @DisplayName("존재하지 않는 BinaryContent 삭제 시도 시 예외가 발생한다.")
    void deleteNonExistentThrowsException() {
        // given
        UUID randomId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> binaryContentService.delete(randomId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("해당 BinaryContent는 없습니다");
    }
}