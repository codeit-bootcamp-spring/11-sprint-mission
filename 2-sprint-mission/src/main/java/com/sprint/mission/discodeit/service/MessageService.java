package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto.Response create(MessageDto.CreateRequest request, List<BinaryContentDto.CreateRequest> fileRequests);
    List<MessageDto.Response> findAllByChannelId(UUID channelId);
    MessageDto.Response update(UUID id, MessageDto.UpdateRequest request);
    void delete(UUID id);
}