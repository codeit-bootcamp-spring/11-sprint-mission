package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
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
public class FileUserService implements UserService {

    private final Path directory;

    // 의존성 주입
    private MessageService messageService;
    private ChannelService channelService;

    public FileUserService() {
        this.directory = Paths.get(System.getProperty("user.dir"), "data", "users");
        initDirectory(this.directory);
    }
    @Override
    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }
    @Override
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
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

    // 저장, 불러오기
    private void saveToFile(User user) {
        Path filePath = directory.resolve(user.getId().toString().concat(".ser"));
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(user);
        } catch (IOException e) {
            throw new RuntimeException("User 파일 저장 실패.", e);
        }
    }

    private User loadFromFile(UUID userId) {
        Path filePath = directory.resolve(userId.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("User 파일 불러오기 실패.", e);
        }
    }
    //##############################################################


    @Override
    public void create(User user) {
        Path filePath = directory.resolve(user.getId().toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            saveToFile(user);
        } else {
            System.out.println("이미 생성된 유저입니다");
        }
    }

    @Override
    public User read(UUID userId) {
        User user = loadFromFile(userId);
        if (user != null) {
            return user;
        } else {
            System.out.println("존재하지 않는 유저입니다.");
            return null;
        }
    }

    @Override
    public List<User> readAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            List<User> users = paths
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (User) ois.readObject();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            if (users.isEmpty()) System.out.println("유저가 존재하지 않습니다.");
            return users;
        } catch (IOException e) {
            throw new RuntimeException("유저 목록 조회 중 오류 발생", e);
        }
    }

    @Override
    public void save(User user) {
        Path filePath = directory.resolve(user.getId().toString().concat(".ser"));
        if (Files.exists(filePath)) {
            saveToFile(user);
        } else {
            System.out.println("존재하지 않는 유저입니다.");
        }
    }

    @Override
    public void delete(UUID userId) {
        User user = loadFromFile(userId);
        if (user != null) {
            // 유저가 작성한 메시지도 삭제(Message)
            if (messageService != null) {
                messageService.clearMessagesByUser(userId);
            }

            // 유저가 속한 채널에서 유저 제외(ChannelMember)
            List<UUID> joinedChannels = new ArrayList<>(user.getJoinedChannelId());
            joinedChannels.forEach(channelId -> {
                if (channelId != null) {
                    Channel channel = channelService.read(channelId);
                    if (channel != null) {
                        channel.removeMember(userId);
                        channelService.save(channel);
                    }
                }
            });

            // 유저가 관리자인 채널을 삭제(Channel)
            if (channelService != null) {
                channelService.deleteChannelByAdmin(userId);
            }

            // 유저 최종 삭제
            Path filePath = directory.resolve(userId.toString().concat(".ser"));
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("유저 파일 삭제 실패", e);
            }
        } else {
            System.out.println("존재하지 않는 유저입니다.");
        }
    }
}
