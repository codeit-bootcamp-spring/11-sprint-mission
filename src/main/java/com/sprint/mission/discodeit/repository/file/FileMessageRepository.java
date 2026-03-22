package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class FileMessageRepository implements MessageRepository {

    private final Path directory;

    // 생성자
    public FileMessageRepository() {
        this.directory = Paths.get(System.getProperty("user.dir"), "data", "messages");
        initDirectory(this.directory);
    }

    private void initDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 중 오류가 발생했습니다.", e);
            }
        }
    }

    // 저장 saveToFile
    @Override
    public void save(Message message) {
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

    // 조회 loadFromFile
    @Override
    public Message findById(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            Message message = (Message) ois.readObject();
            if (message.isDeleted()) {
                return null;
            }
            return message;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Message 파일 불러오기 실패.", e);
        }
    }

    // 전체 조회 readAll
    @Override
    public List<Message> findAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            return paths
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
                    .filter(message -> !message.isDeleted())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("메시지 목록 조회 중 오류 발생", e);
        }
    }

    // 삭제 delete
    @Override
    public void deleteById(UUID id) {
        Message message = findById(id);
        if (message != null) {
            message.softDelete();
            save(message);
        }
    }
}
