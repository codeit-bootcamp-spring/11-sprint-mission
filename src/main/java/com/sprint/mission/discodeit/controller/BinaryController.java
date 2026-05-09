package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.exception.StorageException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.exception.binary.BinaryContentNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/binaryContents")
public class BinaryController {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    public BinaryController(BinaryContentService binaryContentService,
                            BinaryContentStorage binaryContentStorage) {
        this.binaryContentService = binaryContentService;
        this.binaryContentStorage = binaryContentStorage;
    }

    @GetMapping(value = "/{binaryContentId}/download")
    public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
        log.info("파일 다운로드 API 요청: binaryContentId={}", binaryContentId);

        BinaryContentDto binaryContent = binaryContentService.find(binaryContentId)
                .orElseThrow(() -> {
                    log.warn("파일 다운로드 실패 - 파일 없음: binaryContentId={}", binaryContentId);
                    return new BinaryContentNotFoundException(binaryContentId);
                });

        log.debug("파일 다운로드 대상: id={}, fileName={}, contentType={}, size={}",
                binaryContent.id(),
                binaryContent.fileName(),
                binaryContent.contentType(),
                binaryContent.size()
        );

        return binaryContentStorage.download(binaryContent);
    }

    @GetMapping
    public List<BinaryContentDto> findAllByIdIn(@RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
        log.debug("파일 목록 조회 API 요청: count={}",
                binaryContentIds == null ? 0 : binaryContentIds.size()
        );
        return binaryContentService.findAllByIdIn(binaryContentIds);
    }

    @GetMapping(value = "/{binaryContentId}")
    public ResponseEntity<byte[]> findById(@PathVariable UUID binaryContentId) {
        log.info("파일 조회 API 요청: binaryContentId={}", binaryContentId);

        BinaryContentDto binaryContent = binaryContentService.find(binaryContentId)
                .orElseThrow(() -> {
                    log.warn("파일 조회 실패 - 파일 없음: binaryContentId={}", binaryContentId);
                    return new NotFoundException("파일을 찾을 수 없습니다.");
                });

        try (var inputStream = binaryContentStorage.get(binaryContentId)) {
            byte[] bytes = inputStream.readAllBytes();
            log.debug("파일 조회 완료: binaryContentId={}, size={}", binaryContentId, bytes.length);
            return ResponseEntity.ok()
                    .header("Content-Type", binaryContent.contentType())
                    .body(bytes);
        } catch (Exception e) {
            log.error("파일 조회 중 오류 발생: binaryContentId={}", binaryContentId, e);
            throw new StorageException("파일 조회 중 오류가 발생했습니다.", e);
        }
    }
}