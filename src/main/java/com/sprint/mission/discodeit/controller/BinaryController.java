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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
public class BinaryController {

    private final BinaryContentService binaryContentService;

    public BinaryController(BinaryContentService binaryContentService) {
        this.binaryContentService = binaryContentService;
    }

    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public BinaryContentDto download(@PathVariable UUID binaryContentId) {
        return binaryContentService.find(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContentDto> findAllByIdIn(@RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
        return binaryContentService.findAllByIdIn(binaryContentIds);
    }
}