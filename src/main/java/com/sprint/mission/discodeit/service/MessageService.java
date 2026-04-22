package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateParam;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    MessageDto create(MessageCreateRequest request);
    Optional<MessageDto> find(UUID id);
    PageResponse<MessageDto> findAllByChannelId(UUID channelId, int page);
    MessageDto update(MessageUpdateParam param);
    void delete(UUID id);
}