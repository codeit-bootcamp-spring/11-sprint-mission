package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binaryContent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binaryContent.InvalidBinaryContentRequestException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;

    @Override
    public void create(BinaryContentCreateRequest request) {
        byte[] bytes = request.getBytes();
        String fileName = request.getFileName();
        String contentType = request.getContentType();

        if (bytes == null || bytes.length == 0) {
            throw new InvalidBinaryContentRequestException("파일 생성 실패: 파일 데이터가 비어있습니다.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new InvalidBinaryContentRequestException("파일 생성 실패: 파일 이름이 누락되었습니다.");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidBinaryContentRequestException("파일 생성 실패: 파일 타입(ContentType)이 누락되었습니다.");
        }

        BinaryContent binaryContent = new BinaryContent(bytes, fileName, contentType);
        binaryContentRepository.save(binaryContent);
    }

    @Override
    public BinaryContent read(UUID id) {
        if (id == null) {
            throw new InvalidBinaryContentRequestException("조회할 파일의 ID가 입력되지 않았습니다.");
        }

        BinaryContent content = binaryContentRepository.findById(id);
        if (content == null) {
            throw new BinaryContentNotFoundException("조회할 파일을 찾을 수 없습니다. (ID: " + id + ")");
        }
        return content;
    }

    @Override
    public List<BinaryContent> readAllByIdIn(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return binaryContentRepository.findAll().stream()
                .filter(content -> ids.contains(content.getId()))
                .toList();
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new InvalidBinaryContentRequestException("삭제할 파일의 ID가 입력되지 않았습니다.");
        }

        if (binaryContentRepository.findById(id) == null) {
            throw new BinaryContentNotFoundException("삭제할 파일을 찾을 수 없습니다. (ID: " + id + ")");
        }

        binaryContentRepository.deleteById(id);
    }
}