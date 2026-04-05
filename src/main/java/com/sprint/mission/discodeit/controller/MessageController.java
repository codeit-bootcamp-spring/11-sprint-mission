package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> create(@RequestBody MessageCreateRequest dto) {
        Message m = messageService.create(dto);
        MessageResponse messageResponse = new MessageResponse(m.getId(), m.getUserId(), m.getChannelId(), m.getContent(), m.getAttachmentIds(), m.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<MessageResponse> update(@PathVariable UUID id,
                                                  @RequestBody MessageUpdateRequest dto) {
        MessageResponse messageResponse = messageService.update(id, dto);
        return ResponseEntity.ok(messageResponse);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MessageResponse>> findAll(@RequestParam("channelId") UUID channelId) {
        List<MessageResponse> messageResponseList = messageService.findAllByChannelId(channelId);
        return ResponseEntity.ok(messageResponseList);
    }
}
