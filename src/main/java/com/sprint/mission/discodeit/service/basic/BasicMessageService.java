package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.messagedto.CreateMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.DeleteMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageInfoDto;
import com.sprint.mission.discodeit.dto.messagedto.UpdateMessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {


    private final MessageRepository messageRepository;
    private final BinaryContentRepository binaryContentRepository;


    @Override
    public MessageInfoDto create(CreateMessageDto createMessageDto) {


        Message message = new Message(


                createMessageDto.userId(),
                createMessageDto.channelId(),
                createMessageDto.content(),
                null


        );
        if(createMessageDto.binaryFile() != null)
            createMessageDto.binaryFile().forEach(binaryFile -> {
                binaryContentRepository.saveBinaryContent(new BinaryContent(
                    createMessageDto.userId(),
                    message.getId(),
                    BinaryContent.Type.IMAGE,
                    binaryFile
                ));

            });

        //binaryContent id 리스트 뽑아서
        List<UUID> attachmentIds = binaryContentRepository.getAllByMessageId(message.getId()).stream()
                .map(BinaryContent::getId)
                .toList();
        //콘텐츠 리스트 수정
        message.updateAttachmentIds(attachmentIds);
        messageRepository.saveMessage(message);


        return messageToInfo(message);
    }

    @Override
    public MessageInfoDto find(UUID messageId) {

        Message message = messageRepository.getMessage(messageId).orElseThrow();
        return messageToInfo(message);

    }

    @Override
    public List<MessageInfoDto> findAllById(UUID channelId) {
        return messageRepository.getAllByChannelId(channelId)
                .stream()
                .map(this::messageToInfo)
                .toList();
    }

    @Override
    public boolean updateMessage(UpdateMessageDto updateMessageDto) {

        Message message = messageRepository.getMessage(updateMessageDto.messageId()).orElseThrow();

        message.updateMessage(updateMessageDto.content());

        //이전 삭제
        binaryContentRepository.getAllByMessageId(updateMessageDto.messageId()).forEach(binaryContent -> {
            binaryContentRepository.deleteBinaryContent(binaryContent.getId());
        });

        //새로 생성
        if(updateMessageDto.binaryFile() != null)
            updateMessageDto.binaryFile().forEach(binaryFile -> {
                binaryContentRepository.saveBinaryContent(new BinaryContent(
                        message.getSenderId(),
                        message.getId(),
                        BinaryContent.Type.IMAGE,
                        binaryFile
                ));

            });

        //binaryContent id 리스트 뽑아서
         List<UUID> attachmentIds = binaryContentRepository.getAllByMessageId(updateMessageDto.messageId()).stream()
                .map(BinaryContent::getId)
                 .toList();
         //콘텐츠 리스트 수정
        message.updateAttachmentIds(attachmentIds);

        messageRepository.saveMessage(message);

        return true;
    }

    @Override
    public boolean deleteMessage(DeleteMessageDto deleteMessageDto) {


        if(!messageRepository.isExistMessage(deleteMessageDto.messageId()))
            throw new NonExistException("해당 메시지가 존재하지 않습니다.");

        messageRepository.deleteMessage(deleteMessageDto.messageId());
        binaryContentRepository.deleteBinaryContentByMessageId(deleteMessageDto.messageId());

        return true;
    }


    MessageInfoDto messageToInfo(Message message){

        return new MessageInfoDto(

                message.getId(),
                message.getSenderId(),
                message.getChannelId(),
                message.getMessage(),
                binaryContentRepository.getAllByMessageId(message.getId()).stream().toList()
        );
    }










}