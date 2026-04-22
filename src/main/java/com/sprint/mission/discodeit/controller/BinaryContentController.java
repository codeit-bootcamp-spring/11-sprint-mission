package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @GetMapping
  public ResponseEntity<List<BinaryContentDto>> findBinaryContentList(
      @RequestParam List<UUID> binaryContentIds) {

    List<BinaryContentDto> binaryContentDtoList;
    binaryContentDtoList = new ArrayList<>();
    for (UUID id : binaryContentIds) {
      binaryContentDtoList.add(binaryContentService.find(id));
    }

    return ResponseEntity.status(200).body(binaryContentDtoList);
  }

  @Override
  @GetMapping(value = "/{binaryContentId}")
  public ResponseEntity<BinaryContentDto> findBinaryContent(
      @PathVariable UUID binaryContentId) {
    return ResponseEntity.status(200).body(binaryContentService.find(binaryContentId));
  }

  @Override
  @GetMapping(value = "/{binaryContentId}/download")
  public ResponseEntity<?> downloadBinaryContent(@PathVariable UUID binaryContentId) {

    BinaryContentDto dto = binaryContentService.find(binaryContentId);
    return binaryContentStorage.download(dto);


  }


}
