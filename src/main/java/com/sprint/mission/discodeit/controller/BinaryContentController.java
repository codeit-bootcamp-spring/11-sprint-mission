package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.common.RestResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
@RestController
public class BinaryContentController {
    private final BinaryContentService binaryContentService;

    @GetMapping(path = "{binaryContentId}")
    public ResponseEntity<RestResponse> findById(@PathVariable UUID binaryContentId) {
        BinaryContentResponse binaryContent = this.binaryContentService.findById(binaryContentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(binaryContent));
    }

    @GetMapping
    public ResponseEntity<RestResponse> findAllByIdIn(@RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
        List<BinaryContentResponse> binaryContents = this.binaryContentService.findAllByIdIn(binaryContentIds);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(binaryContents));
    }
}
