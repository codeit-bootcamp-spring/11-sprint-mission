package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;


    @Override
    public MessageDto.Response create(MessageDto.CreateRequest request,
                                      List<BinaryContentDto.CreateRequest> fileRequests) {
        if (!channelRepository.existsById(request.channelId())) {
            throw new BusinessException(ErrorCode.CHANNEL_NOT_FOUND);
        }
        if (!userRepository.existsById(request.authorId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<UUID> attachmentIds = fileRequests.stream()
                .map(fileRequest -> {
                    BinaryContent binaryContent = fileRequest.toEntity();

                    BinaryContent createdBinaryContent = binaryContentRepository.save(binaryContent);
                    return createdBinaryContent.getId();
                })
                .toList();

        Message message = request.toEntity(attachmentIds);

        return MessageDto.Response.of(messageRepository.save(message));
    }


    @Override
    public List<MessageDto.Response> findAllByChannelId(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new BusinessException(ErrorCode.CHANNEL_NOT_FOUND);
        }

        return messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .map(MessageDto.Response::of)
                .toList();
    }

    @Override
    public MessageDto.Response update(UUID id, MessageDto.UpdateRequest request) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        message.update(request.content());
        messageRepository.save(message);

        return MessageDto.Response.of(message);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        message.getAttachmentIds().forEach(binaryContentRepository::deleteById);

        messageRepository.deleteById(id);
    }
}