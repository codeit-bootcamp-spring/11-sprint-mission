package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepo;
    private final ChannelRepository channelRepo;
    private final UserRepository userRepo;
    private final BinaryContentRepository binaryContentRepo;
    private final MessageMapper messageMapper;
    private final BinaryContentStorage binaryContentStorage;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest dto, List<MultipartFile> attachments) {
        Channel channel = channelRepo.findById(dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        User author = userRepo.findById(dto.authorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<BinaryContent> binaryContents = saveAttachments(attachments);

        Message message = new Message(dto.content(), channel, author, binaryContents);
        messageRepo.save(message);

        return messageMapper.toDto(message);
    }

    @Override
    public MessageDto findById(UUID id) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));
        return messageMapper.toDto(message);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID id, int page) {
        Channel channel = channelRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, 50);
        var slice = messageRepo.findAllByChannelOrderByCreatedAtDesc(channel, pageable);
        // n+1 문제 해결 필요
        return pageResponseMapper.toDto(slice, messageMapper::toDto);
    }

    @Override
    @Transactional
    public void update(UUID id, MessageUpdateRequest dto) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        message.update(dto.newContent());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        // n+1 문제 해결 필요
        for(BinaryContent attachment : message.getAttachments()) {
            binaryContentStorage.deleteById(attachment.getId());
            binaryContentRepo.delete(attachment);
        }

        messageRepo.delete(message);
    }

    private List<BinaryContent> saveAttachments(List<MultipartFile> attachments) {
        if(attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
                .filter(p -> !p.isEmpty())
                .map(this::saveAttachment)
                .toList();
    }

    private BinaryContent saveAttachment(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            BinaryContent binaryContent = new BinaryContent(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    (long) bytes.length
            );
            binaryContentRepo.save(binaryContent);
            binaryContentStorage.put(binaryContent.getId(), bytes);

            return binaryContent;
        } catch (IOException e){
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }
}
