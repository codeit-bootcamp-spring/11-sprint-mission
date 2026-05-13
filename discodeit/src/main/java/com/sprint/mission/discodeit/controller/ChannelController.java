package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.dto.ChannelUpdateApiRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.service.dto.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.UpdateChannelRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping
    public List<ChannelDto> findAllByUserId(@RequestParam UUID userId) {
        return channelService.findAllByUserId(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/public")
    public ChannelDto createPublic(@Valid @RequestBody CreatePublicChannelRequest request) {
        log.debug("공개 채널 생성 요청: name={}", request.name());
        return channelService.createPublicChannel(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/private")
    public ChannelDto createPrivate(@Valid @RequestBody CreatePrivateChannelRequest request) {
        log.debug("비공개 채널 생성 요청: participantIds={}", request.participantIds());
        return channelService.createPrivateChannel(request);
    }

    @PatchMapping("/{channelId}")
    public ChannelDto update(
            @PathVariable UUID channelId,
            @Valid @RequestBody ChannelUpdateApiRequest request
    ) {
        log.debug("채널 수정 요청: channelId={}", channelId);
        return channelService.update(new UpdateChannelRequest(
                channelId,
                request.newName(),
                request.newDescription()
        ));
    }

    @DeleteMapping("/{channelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID channelId) {
        log.debug("채널 삭제 요청: channelId={}", channelId);
        channelService.delete(channelId);
    }
}
