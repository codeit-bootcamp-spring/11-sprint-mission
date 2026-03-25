package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public BinaryContent createBinaryContent(BinaryContentCreateDto request) {
        if(request.data() == null || request.data().length == 0) {
            throw new IllegalArgumentException("파일 데이터가 비어있습니다.");
        }

        BinaryContent content = BinaryContent.builder()
                .id(UUID.randomUUID())
                .data(request.data())
                .createdAt(Instant.now())
                .messageId(null)
                .build();

        return binaryContentRepository.save(content);
    }

    @Override
    public BinaryContent find(UUID id) {
        return binaryContentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 콘텐츠가 존재하지 않습니다."));
    }

    @Override
    public List<BinaryContent> findAllByIdIn(Collection<UUID> ids) {
        if(ids == null || ids.isEmpty()) {
            System.out.println("❌목록이 비어있습니다");
            return Collections.emptyList();
        }

        return binaryContentRepository.findAllById(ids);
    }

    @Override
    public void deleteBinaryContent(UUID id) {
        binaryContentRepository.findById(id)
                .ifPresent(content -> {
                    binaryContentRepository.delete(id);
                    System.out.println("✅ 바이너리 콘텐츠 삭제 완료");
                });
    }
}
