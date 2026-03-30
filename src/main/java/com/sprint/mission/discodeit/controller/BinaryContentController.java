package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary-content")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(method = RequestMethod.GET, value = "/{contentId}")
    public ResponseEntity<byte[]> getBinaryContent(@PathVariable UUID contentId) {
        BinaryContent content = binaryContentService.read(contentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(content.getContentType()));
        headers.setContentDispositionFormData("attachment", content.getFileName());

        return new ResponseEntity<>(content.getBytes(), headers, HttpStatus.OK);
    }

    // 바이너리 파일 다건 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> getBinaryContents(@RequestParam List<UUID> contentIds) {
        List<BinaryContent> contents = binaryContentService.readAllByIdIn(contentIds);
        return ResponseEntity.ok(contents);
    }
}
