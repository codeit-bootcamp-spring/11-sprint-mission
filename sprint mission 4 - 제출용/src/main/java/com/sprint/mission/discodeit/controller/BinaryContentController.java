package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContent")
@RequiredArgsConstructor
public class BinaryContentController {
    private final BinaryContentService binaryContentService;

    // read
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> read(@PathVariable UUID BinaryContent){
        return ResponseEntity.ok(binaryContentService.read(BinaryContent));
    }

    // readAllByIdIn
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> readAllByIdIn(@RequestParam List<UUID> ids){
        return ResponseEntity.ok(binaryContentService.readAllByIdIn(ids));
    }
}
