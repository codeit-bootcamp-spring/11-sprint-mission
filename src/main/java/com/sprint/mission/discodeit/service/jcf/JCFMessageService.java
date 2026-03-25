//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.repository.MessageRepository;
//import com.sprint.mission.discodeit.service.MessageService;
//
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//public class JCFMessageService implements MessageService {
//    private MessageRepository messageRepository;
//
//    public JCFMessageService(MessageRepository messageRepository) {
//        this.messageRepository = messageRepository;
//    }
//
//    @Override
//    public Message createMessage(UUID channelId, UUID authorId, String message) {
//        Message mes = new Message(channelId, authorId, message);
//        messageRepository.save(mes);
//        System.out.println(mes.getCreatedAt() + " 채널" + channelId + "에 사용자 " + authorId + "님의 메시지가 전송되었습니다.");
//        return mes;
//    }
//
//    @Override
//    public Message readMessage(UUID id) {
//        return messageRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 메세지가 존재하지 않습니다."));
//    }
//
//    @Override
//    public List<Message> readMessagesByChannel(UUID channelId) {
//        return messageRepository.findAll().stream()
//                .filter(m -> m.getChannelId().equals(channelId))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Message> readAllMessage() {
//        return messageRepository.findAll();
//    }
//
//    @Override
//    public void deleteMessage(UUID id) {
//        Message mes = readMessage(id);
//        messageRepository.delete(mes);
//        System.out.println("메시지가 성공적으로 삭제되었습니다.");
//    }
//
//    @Override
//    public void updateMessage(UUID id, String newMessage) {
//        Message mes = readMessage(id);
//        mes.update(newMessage);
//
//        messageRepository.save(mes);
//    }
//}
