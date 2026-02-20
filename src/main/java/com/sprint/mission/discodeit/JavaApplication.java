package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

import java.util.List;
import java.util.UUID;

public class JavaApplication {
    public static void main(String[] args) {
        JCFUserService userService = new JCFUserService();
        JCFMessageService messageService = new JCFMessageService();
        JCFChannelService channelService = new JCFChannelService();

        User userA = new User("Harry", "HarryId", "HarryPw", "Harry@hogwart.com");
        User userB = new User("Hermione", "HermioneId", "HermionePw", "Hermione@hogwart.com");
        User userC = new User("Ron", "RonId", "RonPw", "Ron@hogwart.com");

        Channel channelA = new Channel("Gryffindor", List.of(userA, userB, userC));
        Channel channelB = new Channel("Slytherin", List.of(userA, userB));

        Message messageA = new Message("Hello World!", userA, channelA);
        Message messageB = new Message("Goodbye World!", userB, channelB);

        // Test createUser, findUser, findAllUser
        userService.createUser(userA);
        userService.createUser(userB);
        System.out.println("=== Created Harry, Hermione ===");
        System.out.println();

        UUID firstId = userA.getId();
        System.out.println("Harry's UUID: " + firstId);
        System.out.println("This UUID owner: " + userService.findUser(userA.getId()).getName());
        System.out.println();

        System.out.println("=== Showing All Users ===");
        userService.findAllUser()
                        .forEach(System.out::println);
        System.out.println();

        // Add channels and messages
        channelService.createChannel(channelA);
        channelService.createChannel(channelB);
        messageService.createMessage(messageA);
        messageService.createMessage(messageB);
        System.out.println("=== Add channels and Messages ===");
        System.out.println();

        System.out.println("=== Showing All Channels ===");
        channelService.findAllChannel()
                .forEach(System.out::println);
        System.out.println();

        System.out.println("=== Showing All Messages ===");
        messageService.findAllMessage()
                .forEach(System.out::println);
        System.out.println();

        // Test createUser in middle
        userService.createUser(userC);
        System.out.println("=== Created Ron ===");
        System.out.println();

        System.out.println("=== Showing All Users ===");
        userService.findAllUser()
                .forEach(System.out::println);
        System.out.println();

        // Test updateUser
        // Test findUser, findAllUser after updateUser
        User myUser = new User("Taehoon Kim","taehoonId", "taehoonPw", "terrypotterk@gmail.com");
        userService.updateUser(userA, myUser);
        System.out.println("=== Updated Harry -> Taehoon Kim ===");
        System.out.println();

        System.out.println("=== Showing All Users ===");
        userService.findAllUser()
                .forEach(System.out::println);
        System.out.println();

        System.out.println("Harry's UUID: " + firstId);
        System.out.println("This UUID owner: " + userService.findUser(userA.getId()).getName());
        System.out.println();

        // Test deleteUser
        userService.deleteUser(userB);
        System.out.println("=== Deleted Hermione ===");
        System.out.println();

        System.out.println("=== Showing All Users ===");
        userService.findAllUser()
                .forEach(System.out::println);
        System.out.println();

    }
}
