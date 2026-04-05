package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.common.RestResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
@RestController
public class BinaryContentController implements BinaryContentApi {

  private final BinaryContentService binaryContentService;

  @GetMapping(path = "{binaryContentId}")
  public ResponseEntity<RestResponse<BinaryContentResponse>> findById(
      @PathVariable UUID binaryContentId) {
    BinaryContentResponse binaryContent = this.binaryContentService.findById(binaryContentId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(RestResponse.ok(binaryContent));
  }

  @GetMapping
  public ResponseEntity<RestResponse<List<BinaryContentResponse>>> findAllByIdIn(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    List<BinaryContentResponse> binaryContents = this.binaryContentService.findAllByIdIn(
        binaryContentIds);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(RestResponse.ok(binaryContents));
  }
}
