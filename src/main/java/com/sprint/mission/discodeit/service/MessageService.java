package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.messagedto.CreateMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageInfoDto;
import com.sprint.mission.discodeit.dto.messagedto.UpdateMessageDto;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  MessageInfoDto create(CreateMessageDto createMessageDto, List<MultipartFile> files);

  MessageInfoDto find(UUID messageId);

  List<MessageInfoDto> findAllById(UUID channelId);

  boolean updateMessage(UUID messageId, UpdateMessageDto updateMessageDto);

  boolean deleteMessage(UUID messageId);


}
