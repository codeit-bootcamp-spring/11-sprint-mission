package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Message")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @ApiResponse(responseCode = "201", description = "Message가 성공적으로 생성됨")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public MessageDto sendMessage(
      @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    return messageService.create(request, attachments);
  }

  @PatchMapping("/{messageId}")
  public MessageDto updateMessage(@PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request) {
    return messageService.update(messageId, request);
  }

  @ApiResponse(responseCode = "204", description = "Message가 성공적으로 삭제됨")
  @DeleteMapping("/{messageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteMessage(@PathVariable UUID messageId) {
    messageService.delete(messageId);
  }

  @GetMapping
  public PageResponse<MessageDto> getMessageByChannelId(
      @RequestParam("channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false)
      @Parameter(schema = @Schema(type = "string", format = "date-time")) Instant cursor,
      @PageableDefault(size = 50, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    return messageService.findAllByChannelId(channelId, cursor, pageable.getPageSize());
  }
}
