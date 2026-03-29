package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
@Profile("service-file")
@RequiredArgsConstructor
public class FileMessageService implements MessageService {

    private final Path directory;

    // 의존성 주입
    private UserService userService;
    private ChannelService channelService;

    public FileMessageService() {
        this.directory = Paths.get(System.getProperty("user.dir"), "data", "messages");
        initDirectory(this.directory);
    }
    //setter
    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Override
    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }


    //폴더 생성
    private void initDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 중 오류가 발생했습니다.", e);
            }
        }
    }

    //저장, 불러오기
    private void saveToFile(Message message) {
        Path filePath = directory.resolve(message.getId().toString().concat(".ser"));
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException("Message 파일 저장 실패.", e);
        }
    }

    private Message loadFromFile(UUID messageId) {
        Path filePath = directory.resolve(messageId.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (Message) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Message 파일 불러오기 실패.", e);
        }
    }
    //##############################################################



    @Override
    public void create(Message message) {
        if (message.getSenderId() == null) {
            System.out.println("유저 id가 유효하지 않습니다.");
            return;
        }
        if (message.getChannelId() == null) {
            System.out.println("채널 id가 유효하지 않습니다.");
            return;
        }
        Path filePath = directory.resolve(message.getId().toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            saveToFile(message);
        } else {
            System.out.println("이미 생성된 메시지입니다");
        }
    }

    @Override
    public Message read(UUID id) {
        Message message = loadFromFile(id);
        if (message != null) {
            return message;
        } else {
            System.out.println("존재하지 않는 메시지입니다.");
            return null;
        }
    }

    @Override
    public List<Message> readAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            List<Message> messages = paths
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (Message) ois.readObject();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            if (messages.isEmpty()) System.out.println("메시지가 존재하지 않습니다.");
            return messages;
        } catch (IOException e) {
            throw new RuntimeException("메시지 목록 조회 중 오류 발생", e);
        }
    }

    @Override
    public void update(UUID messageId, MessageUpdateRequest request) {
        Path filePath = directory.resolve(message.getId().toString().concat(".ser"));
        if (Files.exists(filePath)) {
            saveToFile(message);
        } else {
            System.out.println("존재하지 않는 메시지입니다.");
        }
    }

    @Override
    public void delete(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));
        if (Files.exists(filePath)) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                throw new RuntimeException("메시지 파일 삭제 실패", e);
            }
        } else {
            System.out.println("삭제 할 수 없음(존재하지 않는 id)");
        }
    }

    @Override
    public void clearMessagesInChannel(UUID channelId) {
        // 채널 삭제 시 사용, 해당 채널 UUID를 매개변수로 받아서 채널 내 모든 메시지 삭제
        List<Message> messages = readAll();
        if (messages != null) {
            messages.stream()
                    .filter(message -> message.getChannelId().equals(channelId))
                    .forEach(message -> delete(message.getId()));
        }
    }

    @Override
    public void clearMessagesByUser(UUID userId) {
        List<Message> messages = readAll();
        if (messages != null) {
            messages.stream()
                    .filter(message -> message.getSenderId().equals(userId))
                    .forEach(message -> delete(message.getId()));
        }
    }
}
