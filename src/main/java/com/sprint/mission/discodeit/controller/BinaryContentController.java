package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponseDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "/binary-contents/{id}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentResponseDto> find(@PathVariable UUID id) {
        return ResponseEntity.ok(binaryContentService.find(id));
    }

    @RequestMapping(value = "/binary-contents/search", method = RequestMethod.POST)
    public ResponseEntity<List<BinaryContentResponseDto>> findAllByIdIn(@RequestBody List<UUID> idList) {
        return ResponseEntity.ok(binaryContentService.findAllByIdIn(idList));
    }

}
