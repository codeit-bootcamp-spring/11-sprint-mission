package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachments);
    MessageDto findById(UUID id);
    List<MessageDto> findAllByChannelId(UUID id);
    void update(UUID id, MessageUpdateRequest dto);
    void delete(UUID id);
}
