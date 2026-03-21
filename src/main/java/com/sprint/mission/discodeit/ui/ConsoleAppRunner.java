package com.sprint.mission.discodeit.ui;

import com.sprint.mission.discodeit.dto.auth.LoginRequestDTO;
import com.sprint.mission.discodeit.dto.auth.LoginResponseDTO;
import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.dto.user.*;
import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusRequestDTO;
import com.sprint.mission.discodeit.entity.UserStatusType;
import com.sprint.mission.discodeit.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ConsoleAppRunner implements CommandLineRunner {

    // 모든 서비스 의존성 주입 (Lombok의 @RequiredArgsConstructor 사용)
    private final UserService userService;
    private final AuthService authService;
    private final ChannelService channelService;
    private final MessageService messageService;
    private final UserStatusService userStatusService;

    // 현재 로그인한 사용자 정보 상태 저장
    private UUID loggedInUserId = null;
    private String loggedInUsername = null;
    private UUID loggedInUserStatusId = null;

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=========================================");
        System.out.println(" 🚀 DiscodeIT 콘솔 애플리케이션에 오신 것을 환영합니다! ");
        System.out.println("=========================================");

        while (running) {
            try {
                if (loggedInUserId == null) {
                    running = showMainMenu(scanner);
                } else {
                    showLoggedInMenu(scanner);
                }
            } catch (Exception e) {
                System.out.println("\n[시스템 오류] " + e.getMessage());
                System.out.println("다시 시도해주세요.\n");
            }
        }
        scanner.close();
    }

    // ==========================================
    // 1. 메인 메뉴 (비로그인 상태)
    // ==========================================
    private boolean showMainMenu(Scanner scanner) {
        System.out.println("\n[ 메인 화면 ]");
        System.out.println("1. 회원가입");
        System.out.println("2. 로그인");
        System.out.println("3. 전체 유저 목록 보기");
        System.out.println("0. 프로그램 종료");
        System.out.print("▶ 선택: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> handleSignUp(scanner);
            case "2" -> handleLogin(scanner);
            case "3" -> handleFindAllUsers();
            case "0" -> {
                System.out.println("프로그램을 종료합니다.");
                return false;
            }
            default -> System.out.println("잘못된 입력입니다.");
        }
        return true;
    }

    private void handleSignUp(Scanner scanner) {
        System.out.println("\n--- 회원가입 ---");
        System.out.print("이름(Username): ");
        String username = scanner.nextLine();
        System.out.print("이메일(Email): ");
        String email = scanner.nextLine();
        System.out.print("비밀번호(Password): ");
        String password = scanner.nextLine();

        SignUpRequestDTO req = new SignUpRequestDTO(username, email, password, null);
        SignUpResponseDTO res = userService.signUp(req);
        System.out.println("🎉 가입 성공! 환영합니다, " + res.username() + "님.");
    }

    private void handleLogin(Scanner scanner) {
        System.out.println("\n--- 로그인 ---");
        System.out.print("이름(Username): ");
        String username = scanner.nextLine();
        System.out.print("비밀번호(Password): ");
        String password = scanner.nextLine();

        LoginRequestDTO req = new LoginRequestDTO(username, password);
        LoginResponseDTO res = authService.login(req);

        // 세션 정보 저장
        this.loggedInUserId = res.id();
        this.loggedInUsername = res.username();
        System.out.println("✅ 로그인 성공! (상태: " + res.status() + ")");
    }

    private void handleFindAllUsers() {
        System.out.println("\n--- 전체 유저 목록 ---");
        FindAllUserResponseDTO res = userService.findAllUser();
        if (res.userList().isEmpty()) {
            System.out.println("가입된 유저가 없습니다.");
            return;
        }
        for (FindUserByIdResponseDTO user : res.userList()) {
            System.out.printf("- %s (%s) [상태: %s]\n", user.username(), user.email(), user.status());
        }
    }

    // ==========================================
    // 2. 로그인 후 메인 메뉴
    // ==========================================
    private void showLoggedInMenu(Scanner scanner) {
        System.out.println("\n=========================================");
        System.out.println(" 👤 접속 중: " + loggedInUsername);
        System.out.println("=========================================");
        System.out.println("1. 내 프로필 관리");
        System.out.println("2. 채널 메뉴 (채널 입장/생성/가입 등)");
        System.out.println("0. 로그아웃");
        System.out.print("▶ 선택: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> handleProfileMenu(scanner);
            case "2" -> handleChannelMenu(scanner);
            case "0" -> {
                System.out.println("👋 로그아웃 되었습니다.");
                loggedInUserId = null;
                loggedInUsername = null;
            }
            default -> System.out.println("잘못된 입력입니다.");
        }
    }

    // ==========================================
    // 3. 프로필 관리 메뉴
    // ==========================================
    private void handleProfileMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n[ 프로필 관리 ]");
            System.out.println("1. 내 정보 보기");
            System.out.println("2. 내 정보 수정");
            System.out.println("3. 회원 탈퇴");
            System.out.println("0. 뒤로 가기");
            System.out.print("▶ 선택: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    FindUserByIdResponseDTO user = userService.findUser(loggedInUserId);
                    System.out.println("\nID: " + user.id());
                    System.out.println("이름: " + user.username());
                    System.out.println("이메일: " + user.email());
                    System.out.println("상태: " + user.status());
                }
                case "2" -> {
                    System.out.print("새 이름: ");
                    String newName = scanner.nextLine();
                    System.out.print("새 이메일: ");
                    String newEmail = scanner.nextLine();
                    System.out.print("새 비밀번호: ");
                    String newPassword = scanner.nextLine();

                    UpdateUserInfoRequestDTO req = new UpdateUserInfoRequestDTO(loggedInUserId, newName, newEmail, newPassword, null);
                    UpdateUserInfoResponseDTO res = userService.updateUserInfo(req);
                    loggedInUsername = res.username();
                    System.out.println("✅ 정보가 수정되었습니다.");
                }
                case "3" -> {
                    System.out.print("정말 탈퇴하시겠습니까? (Y/N): ");
                    if (scanner.nextLine().equalsIgnoreCase("Y")) {
                        userService.deleteUser(loggedInUserId);
                        System.out.println("탈퇴가 완료되었습니다. 이용해주셔서 감사합니다.");
                        loggedInUserId = null;
                        loggedInUsername = null;
                        back = true;
                    }
                }
                case "0" -> back = true;
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // ==========================================
    // 4. 채널 관리 메뉴
    // ==========================================
    private void handleChannelMenu(Scanner scanner) {
        boolean back = false;
        while (!back && loggedInUserId != null) {
            System.out.println("\n[ 채널 메뉴 ]");
            System.out.println("1. 내가 가입한 채널 목록 보기");
            System.out.println("2. 입장 가능한 전체 Public 채널 보기");
            System.out.println("3. 새 채널 생성하기 (Public)");
            System.out.println("4. 특정 채널 번호로 입장하기 (채팅 치기)");
            System.out.println("5. 채널 가입하기");
            System.out.println("6. 채널 탈퇴하기");
            System.out.println("0. 뒤로 가기");
            System.out.print("▶ 선택: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    System.out.println("\n--- 내 채널 목록 ---");
                    FindChannelsResponseDTO res = channelService.getUserChannels(loggedInUserId);
                    printChannels(res.list());
                }
                case "2" -> {
                    System.out.println("\n--- 전체 퍼블릭 채널 목록 ---");
                    FindChannelsResponseDTO res = channelService.findAllByUserId(loggedInUserId);
                    printChannels(res.list());
                }
                case "3" -> {
                    System.out.print("채널 이름: ");
                    String name = scanner.nextLine();
                    System.out.print("채널 설명: ");
                    String desc = scanner.nextLine();
                    CreatePublicChannelRequestDTO req = new CreatePublicChannelRequestDTO(loggedInUserId, name, desc);
                    ChannelResponseDTO res = channelService.createPublicChannel(req);
                    System.out.println("✅ 채널이 생성되었습니다. [ID: " + res.channelId() + "]");
                }
                case "4" -> {
                    System.out.print("입장할 채널 ID를 입력하세요: ");
                    String inputId = scanner.nextLine();
                    try {
                        UUID channelId = UUID.fromString(inputId);
                        enterChannel(scanner, channelId); // 채널 입장 (채팅 메뉴로 이동)
                    } catch (IllegalArgumentException e) {
                        System.out.println("올바른 UUID 형식이 아닙니다.");
                    }
                }
                case "5" -> {
                    System.out.print("가입할 채널 ID를 입력하세요: ");
                    try {
                        UUID channelId = UUID.fromString(scanner.nextLine());
                        channelService.joinChannel(new JoinChannelRequestDTO(loggedInUserId, channelId));
                        System.out.println("✅ 채널에 가입되었습니다.");
                    } catch (Exception e) {
                        System.out.println("가입 실패: " + e.getMessage());
                    }
                }
                case "6" -> {
                    System.out.print("탈퇴할 채널 ID를 입력하세요: ");
                    try {
                        UUID channelId = UUID.fromString(scanner.nextLine());
                        channelService.leaveChannel(new LeaveChannelRequestDTO(loggedInUserId, channelId));
                        System.out.println("✅ 채널에서 탈퇴했습니다.");
                    } catch (Exception e) {
                        System.out.println("탈퇴 실패: " + e.getMessage());
                    }
                }
                case "0" -> back = true;
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private void printChannels(List<ChannelResponseDTO> channels) {
        if (channels.isEmpty()) {
            System.out.println("조회된 채널이 없습니다.");
            return;
        }
        for (ChannelResponseDTO ch : channels) {
            System.out.printf("[%s] %s (종류: %s) - %s\n", ch.channelId(), ch.name(), ch.type(), ch.description());
        }
    }

    // ==========================================
    // 5. 특정 채널 내부 (채팅방) 메뉴
    // ==========================================
    private void enterChannel(Scanner scanner, UUID channelId) {
        // 채널 존재 여부 및 권한 확인용으로 한 번 호출
        ChannelResponseDTO channel;
        try {
            channel = channelService.getChannels(channelId);
            // 메세지를 한 번 읽어와봄으로써 내가 속한 채널인지 검증
            messageService.getMessagesByChannel(loggedInUserId, channelId);
        } catch (Exception e) {
            System.out.println("채널에 입장할 수 없습니다: " + e.getMessage());
            return;
        }

        boolean inside = true;
        System.out.println("\n💬 [" + channel.name() + "] 채널에 입장했습니다.");

        while (inside) {
            System.out.println("\n=========================================");
            System.out.println(" 1. 메세지 목록 새로고침 (읽기)");
            System.out.println(" 2. 메세지 보내기");
            System.out.println(" 3. 메세지 삭제하기");
            System.out.println(" 8. 채널 삭제 (방장 권한)");
            System.out.println(" 0. 채널 나가기 (로비로 돌아가기)");
            System.out.println("=========================================");
            System.out.print("▶ 선택: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    GetAllMessagesResponseDTO msgs = messageService.getMessagesByChannel(loggedInUserId, channelId);
                    System.out.println("\n--- 메세지 목록 ---");
                    if (msgs.list().isEmpty()) {
                        System.out.println("메세지가 없습니다.");
                    } else {
                        // 최신순으로 정렬되어 있으므로 역순으로 출력하면 위에서 아래로 시간순으로 보임
                        List<MessageResponseDTO> reversed = new ArrayList<>(msgs.list());
                        Collections.reverse(reversed);
                        for (MessageResponseDTO msg : reversed) {
                            String senderName = "알수없음";
                            try {
                                senderName = userService.findUser(msg.userId()).username();
                            } catch (Exception ignored) {}
                            System.out.printf("[%s] %s\n", senderName, msg.content());
                        }
                    }
                }
                case "2" -> {
                    System.out.print("보낼 메세지: ");
                    String content = scanner.nextLine();
                    SendMessageRequestDTO req = new SendMessageRequestDTO(channelId, loggedInUserId, content, null);
                    messageService.sendMessage(req);
                    System.out.println("✅ 전송 완료!");
                }
                case "3" -> {
                    // 삭제를 위해서는 MessageId가 필요한데, 콘솔 특성상 메세지 리스트를 뿌려줄 때 ID를 같이 띄워주고 입력받는 형태로 고도화 가능
                    System.out.println("⚠️ 현재 콘솔에서는 메세지 ID를 알아야 삭제가 가능합니다.");
                    System.out.print("삭제할 메세지 ID를 입력하세요: ");
                    try {
                        UUID msgId = UUID.fromString(scanner.nextLine());
                        messageService.deleteMessage(new DeleteMessageRequestDTO(msgId, loggedInUserId));
                        System.out.println("✅ 메세지 삭제 완료!");
                    } catch (Exception e) {
                        System.out.println("삭제 실패: " + e.getMessage());
                    }
                }
                case "8" -> {
                    System.out.print("정말 이 채널을 삭제하시겠습니까? (방장만 가능) (Y/N): ");
                    if (scanner.nextLine().equalsIgnoreCase("Y")) {
                        try {
                            channelService.deleteChannel(new DeleteChannelRequestDTO(loggedInUserId, channelId));
                            System.out.println("✅ 채널이 폭파(삭제)되었습니다.");
                            inside = false; // 삭제되었으니 방에서 튕김
                        } catch (Exception e) {
                            System.out.println("삭제 실패: " + e.getMessage());
                        }
                    }
                }
                case "0" -> {
                    System.out.println("채널에서 나갑니다.");
                    inside = false;
                }
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }
}