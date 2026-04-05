package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.FileResponseDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.FileMessage;
import com.sprint.mission.discodeit.repository.FileMessageRepository;
import com.sprint.mission.discodeit.service.FileMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicFileMessageService implements FileMessageService {
    private final FileMessageRepository fileMessageRepository;

    @Override
    public FileResponseDto findById(UUID fileId) {
        FileMessage entity = fileMessageRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
        return new FileResponseDto(entity);
    }

    @Override
    public List<FileResponseDto> findAllByIds(List<UUID> ids) {
        List<FileMessage> entities = fileMessageRepository.findAllByIds(ids);
        return entities.stream()
                .map(FileResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public BinaryContent findBinaryContentById(UUID id) {
        FileMessage entity = fileMessageRepository.findById(id)
                .orElse(null);
        if (entity == null) {
            return null;
        }
        return new BinaryContent(
                "text.png", entity.getContent(),
                entity.getContentType(),
                entity.getUserId(),
                entity.getMessageId()
        );
    }
}
