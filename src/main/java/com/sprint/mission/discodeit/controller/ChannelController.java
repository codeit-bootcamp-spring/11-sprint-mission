package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateParam;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;

import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/public")
    public ChannelDto createPublic(@Valid @RequestBody PublicChannelCreateRequest request) {
        log.info("PUBLIC 채널 생성 API 요청: name={}", request.name()
        );
        return channelService.createPublic(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/private")
    public ChannelDto createPrivate(@Valid @RequestBody PrivateChannelCreateRequest request) {
        log.info("PRIVATE 채널 생성 참여자 목록: participantIds={}",
                request.participantIds()
        );
        return channelService.createPrivate(request);
    }

    @PatchMapping(value = "/{channelId}")
    public ChannelDto update(@PathVariable UUID channelId,
                             @Valid @RequestBody ChannelUpdateRequest request) {
        log.info("채널 수정 API 요청: channelId={}", channelId);
        return channelService.update(new ChannelUpdateParam(channelId, request));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{channelId}")
    public void delete(@PathVariable UUID channelId) {
        log.info("채널 삭제 API 요청: channelId={}", channelId);
        channelService.delete(channelId);
    }

    @GetMapping
    public List<ChannelDto> findAllByUserId(@RequestParam UUID userId) {
        log.debug("사용자별 채널 목록 조회 API 요청: userId={}", userId);
        return channelService.findAllByUserId(userId);
    }
}