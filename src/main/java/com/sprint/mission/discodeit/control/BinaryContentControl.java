package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.BinaryContentCreateDto;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContent")
@RequiredArgsConstructor
public class BinaryContentControl {
    private final BinaryContentService binaryContentService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<BinaryContentResponse> createBinaryContent(
            @RequestBody BinaryContentCreateDto request
            ) {
        BinaryContent createdContent = binaryContentService.createBinaryContent(request);
        BinaryContentResponse response = BinaryContentResponse.from(createdContent);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> findBinaryContent(@RequestParam("binaryContentId") UUID binaryContentId) {
        BinaryContent content = binaryContentService.find(binaryContentId);
        return ResponseEntity.ok(content);
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findMultipleBinaryContents(
            @RequestParam("ids") List<UUID> ids
    ) {
        List<BinaryContent> contents = binaryContentService.findAllByIdIn(ids);
        return ResponseEntity.ok(contents);
    }
}
