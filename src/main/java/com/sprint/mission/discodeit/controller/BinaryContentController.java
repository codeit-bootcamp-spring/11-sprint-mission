package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentApi {

    private final BinaryContentService binaryContentService;
    private final BinaryContentStorage binaryContentStorage;

    @GetMapping(value = "/{binaryContentId}")
    public ResponseEntity<BinaryContentDto> find(
            @PathVariable UUID binaryContentId
    ) {
        BinaryContentDto result = binaryContentService.find(binaryContentId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
            @RequestParam List<UUID> binaryContentIds
    ) {
        List<BinaryContentDto> result = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/{binaryContentId}/download")
    public ResponseEntity<?> download(
            @PathVariable UUID binaryContentId
    ) {
        log.debug("BinaryContent download request received. binaryContentId={}", binaryContentId);
        BinaryContentDto dto = binaryContentService.find(binaryContentId);
        return binaryContentStorage.download(dto);
    }

}
