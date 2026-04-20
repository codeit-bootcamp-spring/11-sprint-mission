package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto; // 추가
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource; // 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/binaryContents")
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService; // 주입 누락 수정
  private final BinaryContentStorage binaryContentStorage;

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<Resource> download(@PathVariable("binaryContentId") UUID binaryContentId) {
    // findDto() 메서드가 Service에 구현되어 있어야 함
    BinaryContentDto dto = binaryContentService.findDto(binaryContentId);
    return binaryContentStorage.download(dto);
  }

  // ... 기존 findAllByIdIn 등 유지
}