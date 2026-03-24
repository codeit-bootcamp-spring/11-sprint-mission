package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@SpringBootApplication
public class DiscodeitApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);

        context.getBean(DiscodeitApplication.class)
                .run(userService, channelService, messageService);
    }

    public void run(
            UserService userService,
            ChannelService channelService,
            MessageService messageService
    ) {
        log.info("--------------------------------------------------");
        log.info("Discodeit Service Test Started. 🚀");
        log.info("--------------------------------------------------");

        log.info("1. User Service Functionality Test:");

        log.info(">> 1-1. Creating User With Profile...");
        UserResponse createdUser1 = userService.createUser(
                new UserCreateRequest("JohnDoe", "johndoe", "johndoe@codeit.com", "password123", "123-456-7890"),
                Optional.of(new BinaryContentCreateRequest(
                        "john-profile".getBytes(StandardCharsets.UTF_8),
                        "john-profile.png",
                        "image/png",
                        "john-profile".getBytes(StandardCharsets.UTF_8).length
                ))
        );
        log.info("created user -> {}", createdUser1);
        log.info("--------------------------------------------------");

        log.info(">> 1-2. Creating User Without Profile...");
        UserResponse createdUser2 = userService.createUser(
                new UserCreateRequest("JaneDoe", "janedoe", "janedoe@codeit.com", "password456", "098-765-4321"),
                Optional.empty()
        );
        log.info("created user -> {}", createdUser2);
        log.info("--------------------------------------------------");

        log.info(">> 1-3. Finding All Users...");
        List<UserResponse> users = userService.findAll();
        log.info("Total Users Created: 2, Total Users Found: {}, matched? {}", users.size(), users.size() == 2);
        log.info("--------------------------------------------------");

        log.info(">> 1-4. Duplicate Username...");
        try {
            userService.createUser(
                    new UserCreateRequest("Johnny", createdUser1.username(), "new-user@codeit.com", "password123", "111-111-1111"),
                    Optional.empty()
            );
            log.info("Duplicate username user created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 1-5. Duplicate Email...");
        try {
            userService.createUser(
                    new UserCreateRequest("Johnny", "johnny", createdUser1.email(), "password123", "111-111-1111"),
                    Optional.empty()
            );
            log.info("Duplicate email user created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 1-6. Invalid Email Format...");
        try {
            userService.createUser(
                    new UserCreateRequest("InvalidEmail", "invalidemail", "invalid-email", "password123", "111-111-1111"),
                    Optional.empty()
            );
            log.info("Invalid email user created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 1-7. Invalid Password Length...");
        try {
            userService.createUser(
                    new UserCreateRequest("ShortPass", "shortpass", "shortpass@codeit.com", "short", "111-111-1111"),
                    Optional.empty()
            );
            log.info("Invalid password user created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 1-8. Invalid Phone Number Format...");
        try {
            userService.createUser(
                    new UserCreateRequest("InvalidPhone", "invalidphone", "invalidphone@codeit.com", "password123", "123"),
                    Optional.empty()
            );
            log.info("Invalid phone number user created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info("2. Channel Service Functionality Test:");

        log.info(">> 2-1. Creating Public Channel...");
        ChannelResponse publicChannel1 = channelService.createPublicChannel(
                new PublicChannelCreateRequest("JavaStudy", "Java study room")
        );
        log.info("created channel -> {}", publicChannel1);
        log.info("--------------------------------------------------");

        log.info(">> 2-2. Creating Another Public Channel...");
        ChannelResponse publicChannel2 = channelService.createPublicChannel(
                new PublicChannelCreateRequest("PythonStudy", "Python study room")
        );
        log.info("created channel -> {}", publicChannel2);
        log.info("--------------------------------------------------");

        log.info(">> 2-3. Finding All Channels By Random User...");
        List<ChannelResponse> channels = channelService.findAllByUserId(UUID.randomUUID());
        log.info("Total Public Channels Created: 2, Total Channels Found: {}, matched? {}", channels.size(), channels.size() == 2);
        log.info("--------------------------------------------------");

        log.info(">> 2-4. Duplicate Public Channel Name...");
        try {
            channelService.createPublicChannel(new PublicChannelCreateRequest("JavaStudy", "duplicated"));
            log.info("Duplicate channel name created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 2-5. Empty Channel Name...");
        try {
            channelService.createPublicChannel(new PublicChannelCreateRequest("", "empty name"));
            log.info("Empty channel name created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 2-6. Private Channel Without Participants...");
        try {
            channelService.createPrivateChannel(new PrivateChannelCreateRequest(List.of()));
            log.info("Private channel without participants created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info("3. Message Service Functionality Test:");

        log.info(">> 3-1. Unknown Sender/Channel...");
        try {
            messageService.createMessage(
                    new MessageCreateRequest("Hello", UUID.randomUUID(), UUID.randomUUID()),
                    List.of(
                            new BinaryContentCreateRequest(
                                    "attachment-data".getBytes(StandardCharsets.UTF_8),
                                    "attachment.txt",
                                    "text/plain",
                                    "attachment-data".getBytes(StandardCharsets.UTF_8).length
                            )
                    )
            );
            log.info("Message with unknown sender/channel created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 3-2. Empty Message Content...");
        try {
            messageService.createMessage(
                    new MessageCreateRequest("", UUID.randomUUID(), UUID.randomUUID()),
                    List.of()
            );
            log.info("Empty message content created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 3-3. Null Message Content...");
        try {
            messageService.createMessage(
                    new MessageCreateRequest(null, UUID.randomUUID(), UUID.randomUUID()),
                    List.of()
            );
            log.info("Null message content created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info("4. Additional Validation Test:");

        log.info(">> 4-1. Empty User Nickname...");
        try {
            userService.createUser(
                    new UserCreateRequest("", "username", "email@codeit.com", "password123", "000-000-0000"),
                    Optional.empty()
            );
            log.info("Empty nickname user created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }
        log.info("--------------------------------------------------");

        log.info(">> 4-2. Empty User Username...");
        try {
            userService.createUser(
                    new UserCreateRequest("nickname", "", "email2@codeit.com", "password123", "000-000-0000"),
                    Optional.empty()
            );
            log.info("Empty username user created. ❌");
        } catch (IllegalArgumentException e) {
            log.info("Expected exception caught. ✅ -> {}", e.getMessage());
        }

        log.info("--------------------------------------------------");
        log.info("Discodeit Service Test Finished. ✅ (Total Scenario: 18)");
        log.info("--------------------------------------------------");
    }
}
