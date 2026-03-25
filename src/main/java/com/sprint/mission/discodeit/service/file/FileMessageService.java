//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.ChannelRepository;
//import com.sprint.mission.discodeit.repository.MessageRepository;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.service.MessageService;
//
//import java.util.UUID;
//
//public class FileMessageService implements MessageService {
//    // 기존 JCF Service의 경우 프로그램 종료 시 메모리에서만 존재하기 때문에 프로그램 종료 시 동시에 데이터가 사라진다.
//    // FileSystem을 통해 데이터를 남겨놓기
//    // 직렬화 : Java 객체 -> 바이트 배열 -> 파일
//    // 역직렬화 : 파일 -> 바이트 배열 -> Java 객체
//
//    // 직렬화(Save -> Create를 저장, Update를 저장, Delete를 저장), 역직렬화(Load -> 불러오기)
//
////    private final Map<UUID, Message> messages = new HashMap<>();
////
////    // 저장 메서드 save(직렬화)
////    public void save() {
////        try (FileOutputStream fos = new FileOutputStream("messages.ser");
////             ObjectOutputStream oos = new ObjectOutputStream(fos);
////        ) {
////            oos.writeObject(messages);
////        }
////        catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
////
////    // 불러오기 메서드 load(역직렬화)
////    public void load() {
////        try (FileInputStream fis = new FileInputStream("messages.ser");
////             ObjectInputStream ois = new ObjectInputStream(fis)) {
////            Map<UUID, Message> loadChannels = (Map<UUID, Message>) ois.readObject();
////            messages.clear(); // 한 번 비우고
////            messages.putAll(loadChannels); // 불러온다.(기존에 있던 데이터까지 같이 로드될 수 있기 때문에)
////        } catch (IOException | ClassNotFoundException e) {
////            e.printStackTrace();
////        }
////
////    }
////
////    public UUID findByContent(String content) {
////        for (Map.Entry<UUID, Message> message : messages.entrySet()) {
////            if (message.getValue().getContent().equals(content)) {
////                return message.getKey();
////            }
////        }
////        return null;
////    }
//
//    private final MessageRepository messageRepository;
//    private final ChannelRepository channelRepository;
//    private final UserRepository userRepository;
//
//    public FileMessageService(MessageRepository messageRepository,
//                              ChannelRepository channelRepository,
//                              UserRepository userRepository) {
//        this.messageRepository = messageRepository;
//        this.channelRepository = channelRepository;
//        this.userRepository = userRepository;
//    }
//
//    // Create
//    @Override
//    public Message create(String content, UUID channelId, UUID userId) {
//        // 저장 로직 분리 전
////        messages.put(message.getId(), message);
////        save();
////        System.out.println("메시지를 생성하였습니다.");
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Channel channel = channelRepository.findById(channelId);
//        User author = userRepository.findById(userId);
//        Message message = Message.create(content, channel, author);
//        messageRepository.insert(message);
//        System.out.println("메시지를 생성하였습니다.");
//        System.out.println();
//
//        return message;
//    }
//
//    // Read
//    @Override
//    public Message readAll(UUID id) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!messages.containsKey(id)) { System.out.println("해당 메시지가 존재하지 않습니다."); }
////        else {
////            Message message = messages.get(id);
////            System.out.println("=====메시지 정보=====\n" + message);
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Message message = messageRepository.findById(id);
//        System.out.println("=====메시지 정보=====\n" + message);
//        System.out.println();
//
//        return message;
//    }
//
//    // Update
//    @Override
//    public Message updateContent(UUID id, String newContent) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!messages.containsKey(id)) { System.out.println("해당 메시지가 존재하지 않습니다."); }
////        else {
////            Message message = messages.get(id);
////            System.out.println("수정 전 메시지 : " + message.getContent());
////            message.updateContent(newContent);
////            System.out.println("수정 후 메시지 : " + message.getContent());
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Message message = messageRepository.findById(id);
//        System.out.println("수정 전 메시지 : " + message.getContent());
//        message.updateContent(newContent);
//        System.out.println("수정 후 메시지 : " + message.getContent());
//        messageRepository.update(message);
//        System.out.println();
//
//        return message;
//    }
//
//    // Delete
//    @Override
//    public void delete(UUID id) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!messages.containsKey(id)) { System.out.println("해당 메시지가 존재하지 않습니다."); }
////        else {
////            Message message = messages.get(id);
////            System.out.println("메시지 \"" + message.getContent() + "\"이(가) 삭제되었습니다.");
////            messages.remove(id);
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Message message = messageRepository.findById(id);
//        System.out.println("메시지 \"" + message.getContent() + "\"이(가) 삭제되었습니다.");
//        messageRepository.delete(id);
//        System.out.println();
//    }
//
//}
