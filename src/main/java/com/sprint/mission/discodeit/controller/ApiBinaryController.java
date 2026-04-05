package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ApiBinaryController {

    private final BinaryContentRepository binaryContentRepository;

    public ApiBinaryController(BinaryContentRepository binaryContentRepository) {
        this.binaryContentRepository = binaryContentRepository;
    }

    @RequestMapping(value = "/api/binaryContent/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> find(@RequestParam UUID binaryContentId) {
        Object result = binaryContentRepository.findById(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        BinaryContent binaryContent = (BinaryContent) result;
        return ResponseEntity.ok(binaryContent);
    }
}