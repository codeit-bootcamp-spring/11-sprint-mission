package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @RequestMapping(value = "/{binaryContentId}/download", method = RequestMethod.GET)
    public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
        BinaryContentDto binaryContent = binaryContentService.find(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        return binaryContentStorage.download(binaryContent);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContentDto> findAllByIdIn(@RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
        return binaryContentService.findAllByIdIn(binaryContentIds);
    }
}