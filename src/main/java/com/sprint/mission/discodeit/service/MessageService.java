package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.messagedto.CreateMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.DeleteMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageInfoDto;
import com.sprint.mission.discodeit.dto.messagedto.UpdateMessageDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    MessageInfoDto create(CreateMessageDto createMessageDto);
    MessageInfoDto find(UUID messageId);
    List<MessageInfoDto> findAllById(UUID channelId);
    boolean updateMessage(UpdateMessageDto updateMessageDto);
    boolean deleteMessage(DeleteMessageDto deleteMessageDto);




}
