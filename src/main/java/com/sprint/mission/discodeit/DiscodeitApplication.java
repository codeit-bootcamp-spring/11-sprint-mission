package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// 예전 main에서는 객체생성, 의존성연결, 테스트실행 모두 담당
// AppConfig - 객체생성 및 연결
// TestDataRunner - 테스트
// DiscodeitApplication - 앱 시작
@SpringBootApplication
public class DiscodeitApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context
                = SpringApplication.run(DiscodeitApplication.class, args);

        UserService userService = context.getBean(UserService.class);
        ChannelService channelService = context.getBean(ChannelService.class);
        MessageService messageService = context.getBean(MessageService.class);
        AuthService authService = context.getBean(AuthService.class);
        ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
        BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);

        runTests(userService, channelService, messageService, authService, readStatusService);
        binaryContentTest(binaryContentService);
    }

    private static void runTests(
            UserService userService,
            ChannelService channelService,
            MessageService messageService,
            AuthService authService,
            ReadStatusService readStatusService
    ) {

        userTest(userService);

        UserDto channelUser1 = createUser(userService,
                "채널테스트유저1",
                "ch1@test.com",
                "1111",
                "테스트1"
        );

        UserDto channelUser2 = createUser(userService,
                "채널테스트유저2",
                "ch2@test.com",
                "2222",
                "테스트2"
        );

        UserDto readUser = createUser(userService,
                "읽기테스트유저",
                "read@test.com",
                "1234",
                "읽기 상태 테스트"
        );

        ChannelResponse readChannel = createPublicChannel(channelService,
                "읽기 테스트 채널", "read status test");


        channelTest(channelService, channelUser1.id(), channelUser2.id());
        messageTest(userService, channelService, messageService);
        authTest(authService);
        readStatusTest(readStatusService, readUser.id(), readChannel.id());
    }

    private static UserDto createUser(
            UserService userService,
            String userName,
            String email,
            String password,
            String statusMessage
    ) {
        return userService.create(new UserCreateRequest(
                userName,
                email,
                password,
                statusMessage,
                null
        ));
    }

    private static ChannelResponse createPublicChannel(
            ChannelService channelService,
            String channelName,
            String description
    ) {
        return channelService.createPublicChannel(
                new PublicChannelCreateRequest(channelName, description)
        );
    }

    private static void userTest(UserService userService) {
        // 유저 테스트
        System.out.println("========== 유저 테스트 ==========");

        UserDto u1 = userService.create(new UserCreateRequest(
                "강우진",
                "dnwls@codeit.com",
                "1234",
                "안녕하세요.",
                null
        ));

        UserDto u2 = userService.create(new UserCreateRequest(
                "박지현",
                "jh@codeit.com",
                "1111",
                "반갑습니다.",
                null
        ));

        UserDto u3 = userService.create(new UserCreateRequest(
                "한교동",
                "han@codeit.com",
                "9999",
                "하이~",
                null
        ));

        System.out.println("유저 ID : " + userService.findById(u1.id()).id());
        System.out.println("유저 ID : " + userService.findById(u2.id()).id());
        System.out.println("유저 ID : " + userService.findById(u3.id()).id());


        System.out.println("유저 정보 : " + userService.findById(u1.id())); // 정보 조회
        System.out.println("유저 정보 : " + userService.findById(u2.id()));
        System.out.println("유저 정보 : " + userService.findById(u3.id()));

        System.out.println("현재 유저 수 : " + userService.findAll().size() + "명"); // 전체 몇명 ?

        userService.update(new UserUpdateRequest(
                u1.id(),
                "강강우우진진",
                u1.email(),
                null,
                u1.statusMessage(),
                null
        ));
        System.out.println("u1 이름 변경 후 : " + userService.findById(u1.id()).userName());

        userService.delete(u1.id()); // 삭제
        // userService.update(u1); 이미 삭제된게 맞아 그레서 update가 안돼
        // 그런데, 진짜 DB랑 연결한게 아니기때문에 값은 존재하는듯 ?

        System.out.println("현재 사용자 수 : " + userService.findAll().size() + "명");

        try {
            System.out.println(userService.findById(u1.id()));
        } catch (Exception e) {
            System.out.println("존재하지 않는 id 입니다.");
        }
    }

    private static void channelTest(ChannelService channelService, UUID userId1, UUID userId2) {
        // 채널 테스트
        System.out.println("========== 채널 테스트 ==========");

        ChannelResponse c1 = channelService.createPublicChannel(
                new PublicChannelCreateRequest("모각코", "모여서각자코딩")
        );

        ChannelResponse c2 = channelService.createPrivateChannel(
                new PrivateChannelCreateRequest(List.of(userId1, userId2))
        );

        System.out.print("1번 채널명 : " + c1.channelName());
        System.out.println(" // " + c1.description());
        System.out.print("2번 채널타입 : " + c2.type());
        System.out.println(" // 참여자 수 : " + c2.participantsIds().size());

        channelService.update(c1.id(), new ChannelUpdateRequest("Code-it", "코드잇"));
        System.out.println("바뀐 1번 채널명 : " + channelService.findById(c1.id()).channelName()); // 조회

        channelService.delete(c1.id());

        try {
            System.out.println(channelService.findById(c1.id()));
        } catch (Exception e) {
            System.out.println("존재하지 않는 채널입니다.");
        }
    }

    private static void messageTest(UserService userService, ChannelService channelService, MessageService messageService) {
        // 메세지 테스트
        System.out.println("========== 메세지 테스트 ==========");

        UserDto u = userService.create(new UserCreateRequest(
                "강우진",
                "dnwls@codeit.com",
                "1234",
                "Hi",
                null
        ));
        ChannelResponse c = channelService.createPublicChannel(
                new PublicChannelCreateRequest("sb11", "스프링백엔드11기"));

        MessageResponse m = messageService.create(
                new MessageCreateRequest(
                        u.id(),
                        c.id(),
                        "반가워",
                        List.of()
                )
        );

        System.out.println(u.userName() + "의 '" + c.channelName() + "' 채널에서 보낸 메세지 : " + m.content());

        userService.update(new UserUpdateRequest(
                u.id(),
                "강우진바보",
                u.email(),
                null,
                u.statusMessage(),
                null
        ));

        MessageResponse updatedMessage = messageService.update(
                m.id(),
                new MessageUpdateRequest("반가워바보 ~")
        );

        UserDto updatedUser = userService.findById(u.id());
        System.out.println(updatedUser.userName() + "의 '" + c.channelName() + "' 채널에서 보낸 메세지 : " + m.content());
        System.out.println("마지막 이름 변경 시각 : " + updatedUser.updatedAt());
        System.out.println("마지막 메세지 변경 시각 : " + m.updatedAt());

        // 일치하는 user id가 없을때 검증
        try {
            messageService.create(
                    new MessageCreateRequest(
                            UUID.randomUUID(),
                            c.id(),
                            "실패",
                            List.of()
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // 존재하지 않는 channel 테스트
        ChannelResponse deletedChannel = channelService.createPublicChannel(
                new PublicChannelCreateRequest("삭제용", "테스트"));
        channelService.delete(deletedChannel.id());

        try {
            messageService.create(
                    new MessageCreateRequest(
                            u.id(),
                            deletedChannel.id(),
                            "실패",
                            List.of()
                    )
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void readStatusTest(
            ReadStatusService readStatusService,
            UUID userId,
            UUID channelId
    ) {
        System.out.println("========== ReadStatus 테스트 ==========");

        ReadStatusResponse readStatus = readStatusService.create(
                new ReadStatusCreateRequest(userId, channelId)
        );

        System.out.println("생성된 ReadStatus ID : " + readStatus.id());
        System.out.println("유저 ID : " + readStatus.userId());
        System.out.println("채널 ID : " + readStatus.channelId());
        System.out.println("초기 lastReadAt : " + readStatus.lastReadAt());

        ReadStatusResponse found = readStatusService.findById(readStatus.id());
        System.out.println("조회 성공 ID : " + found.id());

        System.out.println("유저 기준 전체 조회 개수 : " + readStatusService.findAllByUserId(userId).size());

        ReadStatusResponse updated = readStatusService.update(
                readStatus.id(),
                new ReadStatusUpdateRequest(Instant.now())
        );

        System.out.println("수정된 lastReadAt : " + updated.lastReadAt());
        System.out.println("수정 시각 : " + updated.updatedAt());

        try {
			readStatusService.create(new ReadStatusCreateRequest(userId, channelId));
        } catch (Exception e) {
            System.out.println("중복 생성 검증 성공 : " + e.getMessage());
        }

        readStatusService.delete(readStatus.id());

        try {
            System.out.println(readStatusService.findById(readStatus.id()));
        }catch (Exception e) {
            System.out.println("삭제 후 조회 실패 확인 : " + e.getMessage());
        }
    }

    public static void authTest(AuthService authService) {
        System.out.println("========== 로그인 테스트 ==========");

        UserDto loginUser = authService.login(new LoginRequest(
                "한교동",
                "9999"
        ));

        UserDto loginUser2 = authService.login(new LoginRequest(
                "강우진바보",
                "1234"
        ));

        System.out.println("로그인 성공 : " + loginUser.userName());
        System.out.println("로그인 성공 : " + loginUser2.userName());
    }

    public static void binaryContentTest(BinaryContentService binaryContentService) {
        System.out.println("========== BinaryContent 테스트 ==========");

        BinaryContentResponse binaryContent1 = binaryContentService.create(
                new BinaryContentCreateRequest(
                        "profile.png",
                        "image/png",
                        "hello-image".getBytes()
                )
        );

        BinaryContentResponse binaryContent2 = binaryContentService.create(
                new BinaryContentCreateRequest(
                        "resume.pdf",
                        "application.pdf",
                        "hello-pdf".getBytes()
                )
        );

        System.out.println("생성된 파일 1 ID : " + binaryContent1.id());
        System.out.println("생성된 파일 1 이름 : " + binaryContent1.fileName());
        System.out.println("생성된 파일 1 타입 " + binaryContent1.contentType());
        System.out.println("파일 1 바이트 길이 : " + binaryContent1.bytes().length);

        System.out.println("생성된 파일 2 ID : " + binaryContent2.id());
        System.out.println("생성된 파일 2 이름 : " + binaryContent2.fileName());
        System.out.println("생성된 파일 2 타입 " + binaryContent2.contentType());

        BinaryContentResponse found = binaryContentService.findById(binaryContent1.id());
        System.out.println("단건 조회 성공 : " + found.fileName());

        List<BinaryContentResponse> contents = binaryContentService.findAllByIdIn(
                List.of(binaryContent1.id(), binaryContent2.id())
        );
        System.out.println("ID 목록 조회 개수 : " + contents.size());

        binaryContentService.delete(binaryContent1.id());

        try {
            System.out.println(binaryContentService.findById(binaryContent1.id()));
        }catch (Exception e) {
            System.out.println("삭제 후 조회 실패 확인 : " + e.getMessage());
        }
    }
}
