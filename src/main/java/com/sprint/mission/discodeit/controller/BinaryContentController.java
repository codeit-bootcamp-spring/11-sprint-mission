package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentRepository binaryContentRepository;

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable UUID id) {
    return binaryContentRepository.findById(id)
        .map(bc -> ResponseEntity.ok(Map.of(
            "id", bc.getId(),
            "fileName", bc.getFileName() != null ? bc.getFileName() : "file",
            "contentType",
            bc.getContentType() != null ? bc.getContentType() : "application/octet-stream",
            "bytes", Base64.getEncoder().encodeToString(bc.getContent()),
            "size", bc.getSize()
        )))
        .orElse(ResponseEntity.notFound().build());
  }
}