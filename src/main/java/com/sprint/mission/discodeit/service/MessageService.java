package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    MessageDto create(MessageCreateRequest request);
    Optional<MessageDto> find(UUID id);
    List<MessageDto> findAllByChannelId(UUID channelId);
    MessageDto update(MessageUpdateParam param);
    void delete(UUID id);
}