package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.BinaryContent;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@EnableJpaAuditing
@ActiveProfiles("test")
@DataJpaTest
class BinaryContentRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private BinaryContentRepository binaryContentRepository;

  private BinaryContent content1;
  private BinaryContent content2;

  @BeforeEach
  void setUp() {
    content1 = new BinaryContent("file1.png", 100L, "image/png");
    entityManager.persist(content1);

    content2 = new BinaryContent("file2.jpg", 200L, "image/jpeg");
    entityManager.persistAndFlush(content2);
  }

  @Nested
  @DisplayName("find all by id in")
  class FindAllByIdIn {

    @Test
    @DisplayName("return matching binary contents with given ids")
    void findAllByIdIn_ExistingIds_ReturnsBinaryContents() {
      // when
      List<BinaryContent> foundContents = binaryContentRepository.findAllByIdIn(
          List.of(content1.getId(), content2.getId()));

      // then
      assertThat(foundContents).hasSize(2);
      assertThat(foundContents).extracting(BinaryContent::getId)
          .containsExactlyInAnyOrder(content1.getId(), content2.getId());
    }

    @Test
    @DisplayName("return empty list with non existing ids")
    void findAllByIdIn_NonExistingIds_ReturnsEmpty() {
      // when
      List<BinaryContent> foundContents = binaryContentRepository.findAllByIdIn(
          List.of(UUID.randomUUID()));

      // then
      assertThat(foundContents).isEmpty();
    }

    @Test
    @DisplayName("return empty list with empty id list")
    void findAllByIdIn_EmptyIds_ReturnsEmpty() {
      // when
      List<BinaryContent> foundContents = binaryContentRepository.findAllByIdIn(List.of());

      // then
      assertThat(foundContents).isEmpty();
    }
  }
}