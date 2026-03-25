package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
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
    public BinaryContent create(BinaryContentCreateDto dto) {
        BinaryContent binaryContent;

        // userId가 없을 경우 첨부파일을, 있을 경우 프로필 이미지를 binaryContent로 설정
        if (dto.userId() != null) {
            binaryContent = BinaryContent.userProfileImage(
                    dto.userId(), dto.bytes(), dto.fileName(), dto.fileType()
            );
        } else {
            binaryContent = BinaryContent.messageAttachment(
                    dto.messageId(), dto.bytes(), dto.fileName(), dto.fileType()
            );
        }

        binaryContentRepository.insert(binaryContent);

        return binaryContent;
    }

    @Override
    public BinaryContent find(UUID id) {
        return binaryContentRepository.findById(id);
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return ids.stream().map(binaryContentRepository::findById).toList();
    }

    @Override
    public void delete(UUID id) {
        binaryContentRepository.delete(id);
    }
}