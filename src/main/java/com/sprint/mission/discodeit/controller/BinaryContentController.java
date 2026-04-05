package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/binaryContent")
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<BinaryContent> create(@RequestParam("file") MultipartFile file) throws IOException {
        BinaryContentCreateRequest dto = new BinaryContentCreateRequest(file.getBytes());
        BinaryContent binaryContent = binaryContentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(binaryContent);
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContent> find(@RequestParam("binaryContentId") UUID id) {
        BinaryContent binaryContent = binaryContentService.find(id);
        return ResponseEntity.ok(binaryContent);
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findAll(@RequestParam("uuidList") List<UUID> uuidList) {
        List<BinaryContent> binaryContents = binaryContentService.findAllByIdIn(uuidList);
        return ResponseEntity.ok(binaryContents);
    }
}
