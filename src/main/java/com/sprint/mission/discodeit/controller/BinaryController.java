package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
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
    public BinaryContentDto findById(@PathVariable UUID binaryContentId) {
        log.info("파일 메타데이터 조회 API 요청: binaryContentId={}", binaryContentId);

        return binaryContentService.find(binaryContentId)
                .orElseThrow(() -> {
                    log.warn("파일 메타데이터 조회 실패 - 파일 없음: binaryContentId={}", binaryContentId);
                    return new BinaryContentNotFoundException(binaryContentId);
                });
    }
}
