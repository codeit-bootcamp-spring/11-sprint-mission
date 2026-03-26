package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.repository.file.*;
import com.sprint.mission.discodeit.repository.jcf.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.basic.*;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


// UserRepository userRepository = new FileRepository("data/users.ser");
// UserService userService = new FileService(userRepository);
// 위 부분을 Spring에게 등록해주는 클래스 - AppConfig
// 즉, 객체 관리 및 필요한곳에 사용하게 해주는 부분
// 객체 생성과 의존성 연결을 main에서 하지 않고 Spring설정 클래스로 옮김

@Configuration // 조립할 코드만
@EnableConfigurationProperties(RepositoryProperties.class)
public class AppConfig {

    private RepositoryProperties properties;

    public AppConfig(RepositoryProperties properties) {
        this.properties = properties;
    }

    @Bean
    public UserRepository userRepository() {
        if (isFileRepository()){
            return new FileUserRepository(path("users.ser"));
        }
        return new JCFUserRepository();
    }

    @Bean
    public ChannelRepository channelRepository() {
        if (isFileRepository()){
            return new FileChannelRepository(path("channels.ser"));
        }
        return new JCFChannelRepository();
    }

    @Bean
    public MessageRepository messageRepository() {
        if (isFileRepository()){
            return new FileMessageRepository(path("messages.ser"));
        }
        return new JCFMessageRepository();
    }

    @Bean
    public BinaryContentRepository binaryContentRepository() {
        if(isFileRepository()){
            return new FileBinaryContentRepository(path("binaryContents.ser"));
        }
        return new JCFBinaryContentRepository();
    }

    @Bean
    public UserStatusRepository userStatusRepository() {
        if(isFileRepository()){
            return new FileUserStatusRepository(path("userStatuses.ser"));
        }
        return new JCFUserStatusRepository();
    }

    @Bean
    public ReadStatusRepository readStatusRepository() {
        if(isFileRepository()){
            return new FileReadStatusRepository(path("readStatuses.ser"));
        }
        return new JCFReadStatusRepository();
    }

    @Bean
    public UserService userService(
            UserRepository userRepository,
            BinaryContentRepository binaryContentRepository,
            UserStatusRepository userStatusRepository
    ) {
        return new BasicUserService(userRepository, binaryContentRepository, userStatusRepository);
    }

    @Bean
    public AuthService authService(
            UserRepository userRepository,
            UserStatusRepository userStatusRepository
    ) {
        return new BasicAuthService(userRepository, userStatusRepository);
    }

    @Bean
    public ChannelService channelService() {
        return new BasicChannelService(
                channelRepository(),
                userRepository(),
                readStatusRepository(),
                messageRepository());
    }

    @Bean
    public MessageService messageService() {
        return new BasicMessageService(
                messageRepository(),
                userRepository(),
                channelRepository(),
                binaryContentRepository()
        );
    }

    @Bean
    public ReadStatusService readStatusService(){
        return new BasicReadStatusService(
                readStatusRepository(),
                userRepository(),
                channelRepository()
        );
    }

    @Bean
    public UserStatusService userStatusService(){
        return new BasicUserStatusService(
                userStatusRepository(),
                userRepository()
        );
    }

    @Bean
    BinaryContentService binaryContentService(){
        return new BasicBinaryContentService(binaryContentRepository());
    }

    private boolean isFileRepository(){
        return "file".equalsIgnoreCase(properties.getType());
    }

    private String path(String fileName){
        return properties.getFileDirectory() + "/" + fileName;
    }
}
