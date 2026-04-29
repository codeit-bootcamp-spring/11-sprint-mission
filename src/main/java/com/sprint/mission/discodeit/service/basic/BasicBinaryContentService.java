package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBinaryContentService implements BinaryContentService {

    private final BinaryContentRepository binaryContentRepo;
    private final BinaryContentMapper binaryContentMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    @Transactional
    public BinaryContent create(MultipartFile file) {
        // 다른 서비스 객체에서 이용하는 메서드

        if(file == null || file.isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = file.getBytes();
            BinaryContent binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    (long) bytes.length
            );
            binaryContentRepo.save(binaryContent);
            binaryContentStorage.put(binaryContent.getId(), bytes);
            log.info("BinaryContent uploaded. binaryContentId={}, fileName={}, size={}",
                    binaryContent.getId(), binaryContent.getFileName(), binaryContent.getSize());

            return binaryContent;
        } catch (IOException e){
            log.error("BinaryContent upload failed. fileName={}", file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    @Override
    @Transactional
    public List<BinaryContent> createAll(List<MultipartFile> files) {
        // 다른 서비스 객체에서 이용하는 메서드

        if(files == null || files.isEmpty()) {
            return List.of();
        }

        List<BinaryContent> binaryContents = files.stream()
                .filter(p -> p != null && !p.isEmpty())
                .map(this::create)
                .toList();

        log.debug("BinaryContents uploaded. count={}", binaryContents.size());

        return binaryContents;
    }

    @Override
    public BinaryContentDto find(UUID id) {

        BinaryContent binaryContent = binaryContentRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND));

        return binaryContentMapper.toDto(binaryContent);
    }

    @Override
    public List<BinaryContentDto> findAllByIdIn(List<UUID> idList) {

        List<BinaryContent> binaryContents = binaryContentRepo.findAllById(idList);

        if(binaryContents.size() != idList.size()) {
            throw new BusinessException(ErrorCode.BINARY_CONTENT_NOT_FOUND);
        }

        return binaryContents.stream()
                .map(binaryContentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(BinaryContent binaryContent) {
        // 다른 서비스 객체에서 이용하는 메서드

        if(binaryContent == null) return;

        binaryContentStorage.deleteById(binaryContent.getId());
        binaryContentRepo.delete(binaryContent);

        log.info("BinaryContent deleted. binaryContentId={}, fileName={}",
                binaryContent.getId(), binaryContent.getFileName());
    }

    @Override
    @Transactional
    public void deleteAll(List<BinaryContent> binaryContents) {
        // 다른 서비스 객체에서 이용하는 메서드

        if(binaryContents == null || binaryContents.isEmpty()) return;

        List<BinaryContent> binaryContentList = binaryContents.stream()
                .filter(Objects::nonNull)
                .toList();

        for(BinaryContent binaryContent : binaryContentList) {
            binaryContentStorage.deleteById(binaryContent.getId());
        }
        binaryContentRepo.deleteAll(binaryContentList);
        log.info("BinaryContents deleted. count={}", binaryContentList.size());
    }
}
