package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.CreateBinaryContentRequestDTO;
import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserChannelRepository userChannelRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public MessageDto createMessage(MessageCreateRequest request, List<MultipartFile> attachments) {
        // 일단 채널 가입 기능 없으니깐 끄자
//        userChannelRepository.findByUserIdAndChannelId(request.authorId(), request.channelId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOINED_CHANNEL));

        List<UUID> attachmentIds = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (file.isEmpty()) continue;
                try {
                    CreateBinaryContentRequestDTO fileDto = new CreateBinaryContentRequestDTO(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getBytes()
                    );
                    attachmentIds.add(binaryContentService.create(fileDto).getId());
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.FILE_IO_ERROR);
                }
            }
        }

        Message newMessage = Message.create(request.content(), request.channelId(), request.authorId(), attachmentIds);
        Message savedMessage = messageRepository.save(newMessage);

        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
        channel.updateRecentMessageTime(savedMessage.getCreateAt());
        channelRepository.save(channel);

        return MessageDto.from(savedMessage);
    }

    @Override
    public MessageDto updateMessage(UUID messageId, MessageUpdateRequest request) {
        Message message = getMessage(messageId);

        message.updateContent(request.newContent(), message.getUserId(), message.getAttachmentIds());

        return MessageDto.from(messageRepository.save(message));
    }

    @Override
    public void deleteMessage(UUID messageId) {
        Message message = getMessage(messageId);

        if (message.getAttachmentIds() != null) {
            message.getAttachmentIds().forEach(binaryContentService::delete);
        }

        messageRepository.deleteById(messageId);
    }

    @Override
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(MessageDto::from)
                .toList();
    }

    private Message getMessage(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));
    }
}