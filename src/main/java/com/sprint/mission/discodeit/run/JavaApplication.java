package com.sprint.mission.discodeit.run;

import com.sprint.mission.discodeit.entity.Domain.Channel;
import com.sprint.mission.discodeit.entity.Domain.Message;
import com.sprint.mission.discodeit.entity.Domain.User;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class JavaApplication {
    public static void main(String[] args) throws IOException {
//        restart(); // 시작할 때 파일 초기화 (원할때 주석제거해서 초기화하기)

        // 인터페이스 타입으로 선언 → Repository만 갈아끼우면 JCF ↔ File 전환 가능
        UserService userService = new BasicUserService(new FileUserRepository());
        ChannelService channelService = new BasicChannelService(new FileChannelRepository());
        MessageService messageService = new BasicMessageService(new FileMessageRepository(),userService);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("======================================");
            System.out.println("원하는 기능을 선택하세요");
            System.out.println("1. User");
            System.out.println("2. Channel");
            System.out.println("3. Message");
            System.out.println("4. 저장된 UUID목록");
            System.out.println("5. Exit");
            System.out.print("선택: ");

            String input = br.readLine();
            if (input == null) continue;
            input = input.trim();

            if (input.equals("1")) {
                handleUserMenu(br, userService);
            } else if (input.equals("2")) {
                handleChannelMenu(br, channelService);
            } else if (input.equals("3")) {
                handleMessageMenu(br, messageService, userService);
            } else if (input.equals("4")) {
                printAllIds(userService, channelService, messageService);
            } else if (input.equals("5")) {
                System.out.println("프로그램을 종료합니다.");
                break;
            } else {
                System.out.println("잘못된 입력입니다. 다시 선택하세요.");
            }
        }
    }

    private static void restart() {
        try {
            Files.deleteIfExists(Path.of("users.ser"));
            Files.deleteIfExists(Path.of("Channel.ser"));
            Files.deleteIfExists(Path.of("Message.ser"));
        } catch (IOException e) {
            throw new RuntimeException("데이터 초기화에 실패했습니다.", e);
        }
    }

    private static void handleUserMenu(BufferedReader br, UserService userService) throws IOException {
        while (true) {
            System.out.println("----- [User 메뉴] -----");
            System.out.println("1. User 생성");
            System.out.println("2. User 단건 조회");
            System.out.println("3. User 전체 조회");
            System.out.println("4. User 수정");
            System.out.println("5. User 삭제");
            System.out.println("6. User UUID 목록");
            System.out.println("0. 뒤로가기");
            System.out.print("선택: ");

            String input = br.readLine();
            if (input == null) continue;
            input = input.trim();

            try {
                if (input.equals("1")) {
                    System.out.print("이름: ");
                    String name = br.readLine();
                    System.out.print("닉네임: ");
                    String nickname = br.readLine();
                    UUID id = userService.create(new User(name, nickname));
                    System.out.println("생성 완료. ID = " + id);

                } else if (input.equals("2")) {
                    System.out.print("조회할 User ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    User user = userService.read(id);
                    System.out.println(user != null ? user : "해당 ID의 User가 없습니다.");

                } else if (input.equals("3")) {
                    System.out.println("[User 전체 조회]");
                    userService.readAll().forEach(System.out::println);

                } else if (input.equals("4")) {
                    System.out.print("수정할 User ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    System.out.print("새 이름: ");
                    String name = br.readLine();
                    System.out.print("새 닉네임: ");
                    String nickname = br.readLine();
                    userService.update(id, name, nickname);
                    System.out.println("수정 완료.");
                    System.out.println("수정 결과: " + userService.read(id));

                } else if (input.equals("5")) {
                    System.out.print("삭제할 User ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    userService.delete(id);
                    System.out.println("삭제 완료.");

                } else if (input.equals("6")) {
                    printUser(userService);

                } else if (input.equals("0")) {
                    System.out.println("User 메뉴를 종료합니다.");
                    break;

                } else {
                    System.out.println("잘못된 입력입니다. 다시 선택하세요.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("예외 발생: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("알 수 없는 오류: " + e.getMessage());
            }
        }
    }

    private static void handleChannelMenu(BufferedReader br, ChannelService channelService) throws IOException {
        while (true) {
            System.out.println("----- [Channel 메뉴] -----");
            System.out.println("1. Channel 생성");
            System.out.println("2. Channel 단건 조회");
            System.out.println("3. Channel 전체 조회");
            System.out.println("4. Channel 수정");
            System.out.println("5. Channel 삭제");
            System.out.println("6. Channel UUID 목록");
            System.out.println("0. 뒤로가기");
            System.out.print("선택: ");

            String input = br.readLine();
            if (input == null) continue;
            input = input.trim();

            try {
                if (input.equals("1")) {
                    System.out.print("채널 이름: ");
                    String name = br.readLine();
                    System.out.print("채널 설명: ");
                    String desc = br.readLine();
                    UUID id = channelService.create(new Channel(name, desc));
                    System.out.println("생성 완료. ID = " + id);

                } else if (input.equals("2")) {
                    System.out.print("조회할 Channel ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    Channel channel = channelService.read(id);
                    System.out.println(channel != null ? channel : "해당 ID의 Channel이 없습니다.");

                } else if (input.equals("3")) {
                    System.out.println("[Channel 전체 조회]");
                    channelService.readAll().forEach(System.out::println);

                } else if (input.equals("4")) {
                    System.out.print("수정할 Channel ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    System.out.print("새 채널 이름: ");
                    String name = br.readLine();
                    System.out.print("새 채널 설명: ");
                    String desc = br.readLine();
                    channelService.update(id, name, desc);
                    System.out.println("수정 완료.");
                    System.out.println("수정 결과: " + channelService.read(id));

                } else if (input.equals("5")) {
                    System.out.print("삭제할 Channel ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    channelService.delete(id);
                    System.out.println("삭제 완료.");

                } else if (input.equals("6")) {
                    printChannel(channelService);

                } else if (input.equals("0")) {
                    System.out.println("Channel 메뉴를 종료합니다.");
                    break;

                } else {
                    System.out.println("잘못된 입력입니다. 다시 선택하세요.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("예외 발생: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("알 수 없는 오류: " + e.getMessage());
            }
        }
    }

    private static void handleMessageMenu(BufferedReader br, MessageService messageService, UserService userService) throws IOException {
        while (true) {
            System.out.println("----- [Message 메뉴] -----");
            System.out.println("1. Message 생성");
            System.out.println("2. Message 단건 조회");
            System.out.println("3. Message 전체 조회");
            System.out.println("4. Message 수정");
            System.out.println("5. Message 삭제");
            System.out.println("6. Message UUID 목록");
            System.out.println("0. 뒤로가기");
            System.out.print("선택: ");

            String input = br.readLine();
            if (input == null) continue;
            input = input.trim();

            try {
                if (input.equals("1")) {
                    System.out.print("내용: ");
                    String content = br.readLine();
                    System.out.print("보내는이: ");
                    User sender = userService.read(UUID.fromString(br.readLine()));
                    System.out.print("받는이: ");
                    User receiver = userService.read(UUID.fromString(br.readLine()));
                    UUID id = messageService.create(new Message(content, sender, receiver));
                    System.out.println("생성 완료. ID = " + id);

                } else if (input.equals("2")) {
                    System.out.print("조회할 Message ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    Message message = messageService.read(id);
                    System.out.println(message != null ? message : "해당 ID의 Message가 없습니다.");

                } else if (input.equals("3")) {
                    System.out.println("[Message 전체 조회]");
                    messageService.readAll().forEach(System.out::println);

                } else if (input.equals("4")) {
                    System.out.print("수정할 Message ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    System.out.print("새 내용: ");
                    String content = br.readLine();
                    messageService.update(id, content);
                    System.out.println("수정 완료.");
                    System.out.println("수정 결과: " + messageService.read(id));

                } else if (input.equals("5")) {
                    System.out.print("삭제할 Message ID(UUID): ");
                    UUID id = UUID.fromString(br.readLine());
                    messageService.delete(id);
                    System.out.println("삭제 완료.");

                } else if (input.equals("6")) {
                    printMessage(messageService);

                } else if (input.equals("0")) {
                    System.out.println("Message 메뉴를 종료합니다.");
                    break;

                } else {
                    System.out.println("잘못된 입력입니다. 다시 선택하세요.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("예외 발생: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("알 수 없는 오류: " + e.getMessage());
            }
        }
    }

    private static void printAllIds(UserService userService,
                                    ChannelService channelService,
                                    MessageService messageService) {
        System.out.println("===== [현재 저장된 전체 ID 목록] =====");

        System.out.println("\n[User ID]");
        userService.readAll().forEach(u ->
                System.out.println("id=" + u.getId() + " | name=" + u.getUserName())
        );

        System.out.println("\n[Channel ID]");
        channelService.readAll().forEach(c ->
                System.out.println("id=" + c.getId() + " | name=" + c.getChannelName())
        );

        System.out.println("\n[Message ID]");
        messageService.readAll().forEach(m ->
                System.out.println("id=" + m.getId() + " | sender=" + m.getMessageSender().getUserName() + " | receiver=" + m.getMessageReceiver().getUserName())
        );
        System.out.println("====================================\n");
    }

    public static void printUser(UserService userService) {
        System.out.println("==== [현재 저장된 USER ID 목록] ====");
        userService.readAll().forEach(u ->
                System.out.println("id=" + u.getId() + " | name=" + u.getUserName())
        );
        System.out.println("====================================\n");
    }

    public static void printChannel(ChannelService channelService) {
        System.out.println("==== [현재 저장된 CHANNEL ID 목록] ====");
        channelService.readAll().forEach(c ->
                System.out.println("id=" + c.getId() + " | name=" + c.getChannelName())
        );
        System.out.println("====================================\n");
    }

    public static void printMessage(MessageService messageService) {
        System.out.println("==== [현재 저장된 Message ID 목록] ====");
        messageService.readAll().forEach(m ->
                System.out.println("id=" + m.getId() + " | sender=" + m.getMessageSender().getUserName() + " | receiver=" + m.getMessageReceiver().getUserName() + " | content=" + m.getMessageContent())
        );
        System.out.println("====================================\n");
    }
}