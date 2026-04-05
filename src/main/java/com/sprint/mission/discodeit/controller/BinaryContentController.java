package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentInfoDto;
import com.sprint.mission.discodeit.dto.userdto.CreatedUserDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  @GetMapping
  public ResponseEntity<List<BinaryContentInfoDto>> findBinaryContentList(
      @RequestParam List<UUID> binaryContentIds) {

    List<BinaryContentInfoDto> binaryContentDtoList;
    binaryContentDtoList = new ArrayList<>();
    for (UUID id : binaryContentIds) {
      binaryContentDtoList.add(binaryContentService.find(id));
    }

    return ResponseEntity.status(200).body(binaryContentDtoList);
  }


  @GetMapping(value = "/{binaryContentId}")
  public ResponseEntity<BinaryContentInfoDto> findBinaryContent(
      @PathVariable UUID binaryContentId) {
    return ResponseEntity.status(200).body(binaryContentService.find(binaryContentId));
  }


}
