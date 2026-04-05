package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/binaries")
public class BinaryController {

    private final BinaryContentService binaryContentService;

    public BinaryController(BinaryContentService binaryContentService) {
        this.binaryContentService = binaryContentService;
    }

    @RequestMapping(value = "/{binaryId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(@PathVariable UUID binaryId) {
        BinaryContentDto binaryContent = binaryContentService.find(binaryId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + binaryContent.fileName() + "\""
                )
                .contentType(MediaType.parseMediaType(binaryContent.contentType()))
                .body(binaryContent.bytes());
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContentDto> findAllByIdIn(@RequestParam List<UUID> ids) {
        return binaryContentService.findAllByIdIn(ids);
    }

    @RequestMapping(value = "/api/binaryContent/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findBinaryContent(@RequestParam UUID binaryContentId) {
        BinaryContent binaryContent = (BinaryContent) binaryContentService.findEntity(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        return ResponseEntity.ok(binaryContent);
    }
}