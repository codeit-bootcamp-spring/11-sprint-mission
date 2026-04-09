package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelMessageList;
import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdate;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

  MessageDto createMessage(CreateMessageRequest request, List<MultipartFile> attachments);

  MessageDto readMessage(UUID id);

  List<MessageDto> readMessagesByChannel(UUID channelId);

  List<MessageDto> findAllByChannelId(ChannelMessageList request);

  void deleteMessage(UUID id);

  MessageDto updateMessage(UUID id, MessageUpdate request);
}
