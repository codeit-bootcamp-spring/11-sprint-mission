package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;

  @Override
  public BinaryContentResponse create(BinaryContentCreateRequest request) {
    BinaryContent binaryContent = new BinaryContent(
        request.fileName(),
        request.contentType(),
        request.bytes()
    );

    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
    return BinaryContentResponse.of(savedBinaryContent);
  }

  @Override
  public BinaryContentResponse findById(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id);

    if (binaryContent == null) {
      throw new IllegalStateException("존재하지 않는 BinaryContent입니다.");
    }

    return BinaryContentResponse.of(binaryContent);
  }

  @Override
  public BinaryContentResponse findEntitybyId(UUID binaryContentId) {
    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId);

    if (binaryContent == null) {
      throw new IllegalArgumentException("존재하지 않는 파일입니다.");
    }

    return BinaryContentResponse.of(binaryContent);
  }

  @Override
  public List<BinaryContentResponse> findAllByIdIn(List<UUID> ids) {
    return ids.stream()
        .map(binaryContentRepository::findById)
        .filter(binaryContent -> binaryContent != null)
        // 존재하지 않는 id는 제외
        .map(BinaryContentResponse::of)
        .toList();
  }

  @Override
  public void delete(UUID id) {
    if (binaryContentRepository.findById(id) == null) {
      throw new IllegalStateException("존재하지 않는 BinaryContent입니다.");
    }

    binaryContentRepository.delete(id);
  }
}
