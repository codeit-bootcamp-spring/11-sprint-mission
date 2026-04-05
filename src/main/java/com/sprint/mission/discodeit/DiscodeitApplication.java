package com.sprint.mission.discodeit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
    public class DiscodeitApplication {

        public static void main(String[] args) {
            SpringApplication.run(DiscodeitApplication.class, args);
        }
}


//import com.sprint.mission.discodeit.dto.*;
//import com.sprint.mission.discodeit.dto.channel.ChannelUpdateDto;
//import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateDto;
//import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateDto;
//import com.sprint.mission.discodeit.dto.message.MessageCreateDto;
//import com.sprint.mission.discodeit.dto.message.MessageUpdateDto;
//import com.sprint.mission.discodeit.dto.user.UserCreateDto;
//import com.sprint.mission.discodeit.dto.user.UserUpdateDto;
//import com.sprint.mission.discodeit.entity.*;
//import com.sprint.mission.discodeit.service.*;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.core.io.ClassPathResource;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@SpringBootApplication
//public class DiscodeitApplication {
//
//	public static void main(String[] args) throws IOException {
//
//		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class);
//
//		UserService userService = context.getBean(UserService.class);
//		ChannelService channelService = context.getBean(ChannelService.class);
//		MessageService messageService = context.getBean(MessageService.class);
//
//		BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);
//		ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
//		UserStatusService userStatusService = context.getBean(UserStatusService.class);
//		AuthService authService = context.getBean(AuthService.class);
//
//		List<UUID> users = setupUsers(userService);
//		List<UUID> channels = setupPublicChannels(channelService);
//
//		userTest(userService, binaryContentService, userStatusService, authService);
//		channelTest(channelService, readStatusService, users);
//
//		messageTest(messageService, binaryContentService, users, channels);
//
//	}
//
//	static void userTest(UserService userService, BinaryContentService binaryContentService, UserStatusService userStatusService, AuthService authService) throws IOException {
//		System.out.println();
//		System.out.println("-------------------- 1. User 서비스 테스트 --------------------");
//
//		ClassPathResource resource = new ClassPathResource("pikachu.png");
//		BinaryContent bc1 = binaryContentService.create(new BinaryContentCreateDto(resource.getContentAsByteArray()));
//
//		System.out.println();
//		System.out.println("=== 새로운 유저가 있습니다. ===");
//		User u1 = userService.create(new UserCreateDto("피카츄", "pika@codeit.com", "poke123", bc1.getBytes()));
//		User u2 = userService.create(new UserCreateDto("이브이", "eevee@codeit.com", "poke123", null));
//
//		System.out.println("새로운 유저: " + u1);
//		System.out.println("새로운 유저: " + u2);
//
//		System.out.println();
//		System.out.println("=== 전체 사용자 목록을 출력합니다. ===");
//		System.out.println(userService.findAll());
//
//		System.out.println();
//		System.out.println("=== [" + u1.getName() + "] 님을 조회합니다. ===");
//		System.out.println(userService.findById(u1.getId()));
//
//		System.out.println();
//		System.out.println("=== [" + u1.getName() + "] 님이 프로필을 수정했습니다. ===");
//		System.out.println(userService.update(new UserUpdateDto(u1.getId(), "라이츄", "raichu@codeit.com", "poke123", null)));
//
//		System.out.println();
//		System.out.println("=== [" + u2.getName() + "] 님이 탈퇴를 요청했습니다. ===");
//		userService.delete(u2.getId());
//
//		System.out.println();
//		System.out.println("=== 전체 사용자 목록을 출력합니다. ===");
//		System.out.println(userService.findAll());
//
//		System.out.println();
//		System.out.println("-------------------- 2. Auth 서비스 테스트 --------------------");
//
//		System.out.println();
//		// System.out.println("=== [" + u1.getName() + "] 님이 로그인에 성공했습니다. ==="); // 변수의 생명주기 이슈로 [피카츄]로 출력됨.
//		System.out.println("=== [라이츄] 님이 로그인에 성공했습니다. ===");
//		System.out.println(authService.login(new UserLoginDto("라이츄", "poke123")));
//
//		System.out.println();
//		System.out.println("-------------------- 3. UserStatus 서비스 테스트 --------------------");
//
//		System.out.println();
//		System.out.println("=== [라이츄] 님의 상태를 출력합니다. ===");
//		System.out.println(userStatusService.findByUserId(u1.getId()));
//
//		System.out.println();
//		System.out.println("=== [라이츄] 님의 상태를 업데이트 합니다. ===");
//		System.out.println(userStatusService.updateByUserId(u1.getId()));
//		// UserStatusUpdateDto가 아닌 service를 이용해서 업데이트 함.
//
//	}
//
//	static void channelTest(ChannelService channelService, ReadStatusService readStatusService, List<UUID> users) {
//		System.out.println();
//		System.out.println("-------------------- 4. Channel 서비스 테스트 --------------------");
//
//		System.out.println();
//		System.out.println("=== 새로운 채널이 생성되었습니다. ===");
//		Channel c1 = channelService.createPublicChannel(new PublicChannelCreateDto("행정-공지방", "행정 관련 공지를 올리는 방입니다."));
//		Channel c2 = channelService.createPublicChannel(new PublicChannelCreateDto("이벤트-공지방", "이벤트 공지를 방입니다."));
//		Channel c3 = channelService.createPrivateChannel(new PrivateChannelCreateDto(users));
//
//		System.out.println(c1);
//		System.out.println(c2);
//		System.out.println(c3);
//
//		System.out.println();
//		System.out.println("=== [리피아] 님의 채널을 모두 출력합니다. ===");
//		System.out.println(channelService.findAllByUserId(users.get(0)));
//
//		System.out.println();
//		System.out.println("=== [행정-공지방]을 수정합니다. ===");
//		System.out.println(channelService.update(new ChannelUpdateDto(c1.getId(), "학습-공지방", "학습 관련 공지를 올리는 방입니다.")));
//
//		System.out.println();
//		System.out.println("=== [학습-공지방]을 삭제합니다. ===");
//		channelService.delete(c1.getId());
//
//		System.out.println();
//		System.out.println("=== 삭제를 확인하기 위해 [리피아] 님의 채널을 모두 출력합니다. ===");
//		System.out.println(channelService.findAllByUserId(users.get(0)));
//
//		System.out.println();
//		System.out.println("-------------------- 5. ReadStatus 서비스 테스트 --------------------");
//
//		System.out.println();
//		System.out.println("=== [글레이시아] 님의 읽음 상태를 모두 출력합니다. ===");
//		System.out.println(readStatusService.findAllByUserId(users.get(1)));
//
//		System.out.println();
//		System.out.println("=== [글레이시아] 님이 [이벤트-공지방] 을 읽었습니다. ===");
//		ReadStatus rs1 = readStatusService.create(new ReadStatusCreateDto(users.get(1), c2.getId()));
//
//		System.out.println();
//		System.out.println("=== [글레이시아] 님의 읽음 상태를 모두 출력합니다. ===");
//		System.out.println(readStatusService.findAllByUserId(users.get(1)));
//
//		System.out.println();
//		System.out.println("=== [글레이시아] 님이 [이벤트-공지방] 을 읽었습니다. ===");
//		System.out.println(readStatusService.update(new ReadStatusUpdateDto(users.get(1), c2.getId())));
//
//		System.out.println("=== [글레이시아] 님이 PRIVATE 채팅방 을 읽었습니다. ===");
//		System.out.println(readStatusService.update(new ReadStatusUpdateDto(users.get(1), c3.getId())));
//
//		System.out.println();
//		System.out.println("=== [글레이시아] 님의 읽음 상태를 모두 출력합니다. ===");
//		System.out.println(readStatusService.findAllByUserId(users.get(1)));
//	}
//
//	static void messageTest(MessageService messageService, BinaryContentService binaryContentService, List<UUID> users, List<UUID> channels) throws IOException {
//		System.out.println();
//		System.out.println("-------------------- 6. Message 서비스 테스트 --------------------");
//
//		ClassPathResource resource = new ClassPathResource("Leafeon.png");
//		BinaryContent bc1 = binaryContentService.create(new BinaryContentCreateDto(resource.getContentAsByteArray()));
//
//		System.out.println();
//		System.out.println("=== 새로운 메시지가 생성되었습니다. ===");
//
//		Message m1 = messageService.create(new MessageCreateDto(users.get(0), channels.get(1), "안녕하세요!", List.of(bc1.getBytes())));
//        Message m2 = messageService.create(new MessageCreateDto(users.get(1), channels.get(1), "안녕 안녕", null));
//        Message m3 = messageService.create(new MessageCreateDto(users.get(2), channels.get(0), "음식을 소개할게~", null));
//
//		System.out.println(m1);
//        System.out.println(m2);
//        System.out.println(m3);
//
//		System.out.println();
//		System.out.println("=== [플레이방] 의 모든 메시지를 출력합니다. ===");
//		System.out.println(messageService.findAllByChannelId(channels.get(1)));
//
//		System.out.println();
//		System.out.println("=== [리피아] 님이 [안녕하세요!] 메시지를 수정했습니다. ===");
//		System.out.println(messageService.update(new MessageUpdateDto(m1.getId(), "반가워!")));
//
//		System.out.println();
//		System.out.println("=== [리피아] 님의 [반가워!] 메시지를 삭제합니다. ===");
//		messageService.delete(m1.getId());
//
//		System.out.println();
//		System.out.println("=== 삭제를 확인하기 위해 [플레이방] 의 모든 메시지를 출력합니다. ===");
//		System.out.println(messageService.findAllByChannelId(channels.get(1)));
//	}
//
//	static List<UUID> setupUsers(UserService userService) {
//		ArrayList<UUID> list = new ArrayList<>(3);
//
//		User u1 = userService.create(new UserCreateDto("리피아", "leafeon@codeit.com", "poke1234!", null));
//		User u2 = userService.create(new UserCreateDto("글레이시아", "glaceon@codeit.com", "poke1234!", null));
//		User u3 = userService.create(new UserCreateDto("에브이", "espeon@codeit.com", "poke1234!", null));
//
//		list.add(u1.getId());
//		list.add(u2.getId());
//		list.add(u3.getId());
//
//		return list;
//	}
//
//	static List<UUID> setupPublicChannels(ChannelService channelService) {
//		ArrayList<UUID> list = new ArrayList<>(2);
//		Channel c1 = channelService.createPublicChannel(new PublicChannelCreateDto("음식-소통방", "주변의 맛집을 추천하는 방입니다."));
//		Channel c2 = channelService.createPublicChannel(new PublicChannelCreateDto("플레이방", "신나게 노는 방입니다."));
//
//		list.add(c1.getId());
//		list.add(c2.getId());
//
//		return list;
//	}
//
//}
