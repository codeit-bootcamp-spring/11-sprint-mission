package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary")
public class BinaryContentController {
    private final BinaryContentService binaryContentService;

    public BinaryContentController(BinaryContentService binaryContentService) {
        this.binaryContentService = binaryContentService;
    }

    @RequestMapping(value = "/api/binaryContent/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentDto> getBinaryContent(
            @RequestParam("binaryContentId") UUID binaryContentId) {
        BinaryContentDto content =
                binaryContentService.findById(binaryContentId);
        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/api/binary", method = RequestMethod.GET)
    public List<BinaryContentDto> getMultipleBinaryContent(
            @RequestParam List<UUID> id) {
        return binaryContentService.findAllByIdIn(id);
    }
}
