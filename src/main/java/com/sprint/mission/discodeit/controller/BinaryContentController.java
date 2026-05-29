package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.BinaryContentApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.exception.common.UnexpectedErrorException;
import com.sprint.mission.discodeit.storage.DownloadResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
  private final BinaryContentStorage binaryContentStorage;

  @GetMapping(path = "{binaryContentId}")
  public ResponseEntity<BinaryContentResponse> findById(
      @PathVariable UUID binaryContentId) {
    log.info("binary-content find-by-id request: id={}", binaryContentId);
    BinaryContentResponse binaryContent = this.binaryContentService.findById(binaryContentId);

    log.debug("binary-content find-by-id response: {}", binaryContent);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(binaryContent);
  }

  @GetMapping
  public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    log.info("binary-content find-all-by-id-in request: ids={}", binaryContentIds);
    List<BinaryContentResponse> binaryContents = this.binaryContentService.findAllByIdIn(
        binaryContentIds);

    log.debug("binary-content find-all-by-id-in response: count={}", binaryContents.size());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(binaryContents);
  }

  @GetMapping(path = "{binaryContentId}/download")
  public ResponseEntity<?> download(
      @PathVariable UUID binaryContentId) {
    log.info("binary-content download request: id={}", binaryContentId);
    BinaryContentResponse binaryContent = this.binaryContentService.findById(binaryContentId);

    DownloadResult result = this.binaryContentStorage.download(binaryContent);
    if (result instanceof DownloadResult.Stream s) {
      log.debug("binary-content download response (stream): fileName={}, content-type={}, content-length={}",
          s.fileName(), s.contentType(), s.size());
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION,
              ContentDisposition.attachment().filename(s.fileName()).build().toString())
          .contentType(MediaType.parseMediaType(s.contentType()))
          .contentLength(s.size())
          .body(s.resource());
    } else if (result instanceof DownloadResult.Redirect r) {
      log.debug("binary-content download response (redirect): url={}", r.url());
      return ResponseEntity.status(HttpStatus.FOUND)
          .header(HttpHeaders.LOCATION, r.url())
          .build();
    }
    throw UnexpectedErrorException.withCause();
  }
}
