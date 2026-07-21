package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binary.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BinaryContentDto create(BinaryContentCreateRequest request) {
        log.info("파일 생성 시작: fileName={}, contentType={}, size={}",
                request.fileName(),
                request.contentType(),
                request.bytes() == null ? 0 : request.bytes().length
        );

        BinaryContent binaryContent = new BinaryContent(
                request.fileName(),
                request.bytes() == null ? 0 : request.bytes().length,
                request.contentType()
        );

        BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);

        if (request.bytes() != null) {
            eventPublisher.publishEvent(
                    new BinaryContentCreatedEvent(savedBinaryContent.getId(), request.bytes())
            );
            log.debug("파일 바이너리 저장 완료: binaryContentId={}", savedBinaryContent.getId());
        }

        log.info("파일 생성 완료: binaryContentId={}", savedBinaryContent.getId());

        return binaryContentMapper.toDto(savedBinaryContent);
    }

    @Override
    public Optional<BinaryContentDto> find(UUID id) {
        log.debug("파일 메타데이터 조회: binaryContentId={}", id);

        return binaryContentRepository.findById(id)
                .map(binaryContentMapper::toDto);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
        log.debug("파일 메타데이터 목록 조회: count={}", ids == null ? 0 : ids.size());

        List<BinaryContentDto> result = binaryContentRepository.findAllById(ids).stream()
                .map(binaryContentMapper::toDto)
                .toList();

        log.debug("파일 메타데이터 목록 조회 완료: resultCount={}", result.size());

        return result;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("파일 메타데이터 삭제 시작: binaryContentId={}", id);

        binaryContentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("파일 메타데이터 삭제 실패 - 파일 없음: binaryContentId={}", id);
                    return new BinaryContentNotFoundException(id);
                });

        binaryContentRepository.deleteById(id);

        log.info("파일 메타데이터 삭제 완료: binaryContentId={}", id);
    }

    @Override
    public Optional<BinaryContent> findEntity(UUID id) {
        return binaryContentRepository.findById(id);
    }
}