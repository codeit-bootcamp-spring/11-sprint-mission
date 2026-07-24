package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @Timed("message.create.async")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto.Response> create(
      @Valid @RequestPart("messageCreateRequest") MessageDto.CreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    log.info("메시지 생성 요청: channelId={}, authorId={}", request.channelId(), request.authorId());
    List<BinaryContentDto.CreateRequest> fileRequests = BinaryContentDto.CreateRequest.ofList(
        attachments);
    MessageDto.Response response = messageService.create(request, fileRequests);

    log.debug("메시지 생성 응답: {}", response);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageDto.Response> update(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageDto.UpdateRequest request) {
    log.info("메시지 업데이트 요청: messageId={}", messageId);
    MessageDto.Response response = messageService.update(messageId, request);

    log.debug("메시지 업데이트 응답: {}", response);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID messageId) {
    log.info("메시지 삭제 요청: messageId={}", messageId);
    messageService.delete(messageId);

    log.debug("메시지 삭제 응답 완료");
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageDto.Response>> findAllByChannelId(
      @RequestParam UUID channelId,
      @RequestParam(required = false) Instant cursor,
      @PageableDefault(size = 50, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    log.debug("채널 메시지 목록 조회 요청: channelId={}", channelId);
    PageResponse<MessageDto.Response> responseList = messageService.findAllByChannelId(channelId,
        cursor,
        pageable);

    log.debug("채널 메시지 목록 조회 응답: {}건", responseList.content().size());
    return ResponseEntity.ok(responseList);
  }
}
