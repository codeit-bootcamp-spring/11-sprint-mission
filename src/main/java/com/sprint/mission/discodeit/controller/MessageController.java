package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageDto> create(@Valid @RequestBody MessageCreateRequest dto) {
        MessageDto result = messageService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<Void> update(@PathVariable UUID messageId, @Valid @RequestBody MessageUpdateRequest dto) {
        messageService.update(messageId, dto);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MessageDto>> findAllByChannelId(@RequestParam UUID channelId) {
        List<MessageDto> result = messageService.findAllByChannelId(channelId);
        return ResponseEntity.ok(result);
    }

}
