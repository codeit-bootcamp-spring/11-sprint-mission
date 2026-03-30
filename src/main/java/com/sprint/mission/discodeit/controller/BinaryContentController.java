package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponseDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "/binaryContent/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentResponseDto> find(@RequestParam UUID binaryContentId) {
        return ResponseEntity.ok(binaryContentService.find(binaryContentId));
    }

    @RequestMapping(value = "/binaryContent/search", method = RequestMethod.POST)
    public ResponseEntity<List<BinaryContentResponseDto>> findAllByIdIn(@RequestBody List<UUID> idList) {
        return ResponseEntity.ok(binaryContentService.findAllByIdIn(idList));
    }

}
