package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachments);
    MessageDto findById(UUID id);
    PageResponse<MessageDto> findAllByChannelId(UUID id, int page);
    void update(UUID id, MessageUpdateRequest dto);
    void delete(UUID id);
}
