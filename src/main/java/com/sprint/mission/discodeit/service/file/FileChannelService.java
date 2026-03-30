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
public class FileChannelService implements ChannelService{

    private final Path directory;

    // 의존성 주입
    private MessageService messageService;
    private UserService userService;

    //생성자로 주입
    public FileChannelService(){
        this.directory = Paths.get(System.getProperty("user.dir"), "data", "channels");
        initDirectory(this.directory);
    }
    //setter로 주입
    @Override
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Override
    public void setUserService(UserService userService){
        this.userService = userService;
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
    private void saveToFile(Channel channel) {
        Path filePath = directory.resolve(channel.getId().toString().concat(".ser"));
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new RuntimeException("Channel 파일 저장 실패.", e);
        }
    }

    private Channel loadFromFile(UUID channelId) {
        Path filePath = directory.resolve(channelId.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (Channel) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Channel 파일 불러오기 실패.", e);
        }
    }
//##############################################################







    @Override
    public void create(Channel channel) {
        if(channel.getAdminId() == null){
            System.out.println("어드민 id가 유효하지 않습니다.");
            return;
        }
        Path filePath = directory.resolve(channel.getId().toString().concat(".ser"));
        if(!Files.exists(filePath)) {
            saveToFile(channel);
            User admin = userService.read(channel.getAdminId());
            if (admin != null) {
                admin.joinChannel(channel.getId());
                userService.save(admin);
            }
        } else System.out.println("이미 생성된 채널입니다");

    }

    @Override
    public Channel read(UUID channelId) {
        // 변경: channelData.get() 대신 파일에서 로드
        Channel channel = loadFromFile(channelId);
        if(channel != null){
            return channel;
        } else {
            System.out.println("존재하지 않는 채널입니다.");
            return null;
        }
    }

    @Override
    public List<Channel> readAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            List<Channel> channels = paths
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (Channel) ois.readObject();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();

            if (channels.isEmpty()) System.out.println("채널이 존재하지 않습니다.");
            return channels;
        } catch (IOException e) {
            throw new RuntimeException("채널 목록 조회 중 오류 발생", e);
        }
    }

    @Override
    public void save(Channel channel) {
        Path filePath = directory.resolve(channel.getId().toString().concat(".ser"));

        if(Files.exists(filePath)){
            saveToFile(channel);
        } else {
            System.out.println("존재하지 않는 채널입니다.");
        }
    }

    @Override
    public void delete(UUID channelId) {
        if(channelId == null) return;

        Channel channel = loadFromFile(channelId);
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }

        // 멤버 연결 해제
        List<UUID> disconnectMembers = new ArrayList<>(channel.getMemberId());
        disconnectMembers.forEach(userId -> {
            if(userId != null){
                User user = userService.read(userId);
                if(user != null){
                    user.leaveChannel(channelId);
                    userService.save(user);
                }
            }
        });

        // 메시지 삭제
        if (messageService != null) {
            messageService.clearMessagesInChannel(channelId);
        }


        Path filePath = directory.resolve(channelId.toString().concat(".ser"));
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("채널 파일 삭제 실패", e);
        }
    }
    @Override
    public void deleteChannelByAdmin(UUID adminId){

        List<UUID> channelsToDelete = readAll().stream()
                .filter(channel -> channel.getAdminId().equals(adminId))
                .map(Channel::getId)
                .toList();

        for(UUID channelId : channelsToDelete){
            this.delete(channelId);
        }
    }

    @Override
    public void addUserToChannel(UUID userId, UUID channelId) {
        if(userId == null || channelId == null) return;

        Channel channel = loadFromFile(channelId);
        User user = userService.read(userId);

        if(channel != null && user != null){
            channel.addMember(userId);
            saveToFile(channel);

            user.joinChannel(channelId);
            userService.save(user);
        } else {
            System.out.println("채널이나 유저가 존재하지 않습니다.");
        }
    }

    @Override
    public void removeUserFromChannel(UUID userId, UUID channelId) {
        if(userId == null || channelId == null) return;

        Channel channel = loadFromFile(channelId);
        User user = userService.read(userId);

        if(channel != null && user != null){
            channel.removeMember(userId);
            saveToFile(channel);

            user.leaveChannel(channelId);
            userService.save(user);
        } else {
            System.out.println("채널이나 유저가 존재하지 않습니다.");
        }
    }
}
