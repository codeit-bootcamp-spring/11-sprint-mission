package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.BINARY_CONTENT)
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  // 여러 첨부파일 조회
  @Override
  @GetMapping
  public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
      @RequestParam List<UUID> binaryContentIds) {
    return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
  }

  // Id로 바이너리 파일을 단건 조회
  @Override
  @GetMapping(value = "/{binaryContentId}")
  public ResponseEntity<BinaryContentDto> find(
      @PathVariable(value = "binaryContentId") UUID id) {
    return ResponseEntity.ok(binaryContentService.find(id));
  }

  // 다운로드 API
  @Override
  @GetMapping(value = "{binaryContentId}/download")
  public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
    BinaryContentDto dto = binaryContentService.find(binaryContentId);

    return binaryContentStorage.download(dto);
  }
}
