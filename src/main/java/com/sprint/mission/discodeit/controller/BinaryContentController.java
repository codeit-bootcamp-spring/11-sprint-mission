package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @GetMapping
    public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
            @RequestParam List<UUID> binaryContentIds
    ) {
        List<BinaryContentDto> response = binaryContentService.findAllByIdIn(binaryContentIds)
                .stream()
                .map(BinaryContentDto::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{binaryContentId}")
    public ResponseEntity<BinaryContentDto> find(
            @PathVariable UUID binaryContentId
    ) {
        BinaryContentDto response = BinaryContentDto.from(binaryContentService.find(binaryContentId));
        return ResponseEntity.ok(response);
    }
}