package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.LoginFailedException;
import com.sprint.mission.discodeit.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		AuthService authService = context.getBean(AuthService.class);
		ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
		UserStatusService userStatusService = context.getBean(UserStatusService.class);
		BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);

		System.out.println("===채팅 서비스 테스트===\n");

		UUID user1id = userService.create(new UserCreateRequest("유저1","이메일1", "비번1",null));
		UUID user2id = userService.create(new UserCreateRequest("유저2","이메일2", "비번2",null));
		UUID user3id = userService.create(new UserCreateRequest("유저3","이메일3", "비번3",null));
		UUID user4id = userService.create(new UserCreateRequest("유저4","이메일4", "비번4",null));

		System.out.println("유저 생성 완료");

		//#################################################
		System.out.println("\n===로그인 테스트===\n");
		// 성공 테스트
		try {
			UserResponse loggedInUser = authService.login(new LoginRequest("유저1", "비번1"));
			System.out.println("로그인 성공! 유저 정보: " + loggedInUser.getUserName());
		} catch (LoginFailedException e) {
			System.out.println("로그인 실패: " + e.getMessage());
		}

		// 실패 테스트
		try {
			authService.login(new LoginRequest("유저1", "틀린비번"));
		} catch (LoginFailedException e) {
			System.out.println("로그인 실패 (실패테스트): " + e.getMessage());
		}

		//#################################################
		System.out.println("\n===유저 상태 테스트===\n");
		UserStatus userStatus = new UserStatus(user1id);
		System.out.println("유저1 온라인 상태: " + userStatus.isOnline());


		//#################################################
		System.out.println("\n===유저 조회(단건) 테스트===\n");
		System.out.println(userService.read(user1id));

		//#################################################
		System.out.println("\n===유저 조회(전체) 테스트===\n");
		userService.readAll().forEach(System.out::println);

		//#################################################
		System.out.println("\n===유저 수정 테스트===\n");
		userService.update(user3id, new UserUpdateRequest("수정된 유저3", "수정된 이메일3", "수정된 비번3", null));

		System.out.println("유저3 정보 수정 완료");
		System.out.println(userService.read(user3id));

		//#################################################
		System.out.println("\n===유저 삭제 테스트===\n");
		userService.delete(user4id);
		System.out.println("유저4 삭제 완료");
		userService.readAll().forEach(System.out::println);

		//#################################################
		System.out.println("\n===채널 등록 테스트===\n");
		UUID channel1id = channelService.createPublicChannel(new PublicChannelCreateRequest("채널1", user1id));

		channelService.addUserToChannel(user2id, channel1id);
		channelService.addUserToChannel(user3id, channel1id);
		
		System.out.println("\n===읽기 상태 테스트===\n");
		ReadStatus readStatus = new ReadStatus(user1id, channel1id);
		System.out.println("유저1이 채널1을 읽은 시간: " + readStatus.getUpdatedAt());

		//#################################################
		System.out.println("\n===메세지 등록 테스트===\n");
		messageService.create(new MessageCreateRequest("유저1이 채널1에 작성한 메세지1", user1id, channel1id, null));
		messageService.create(new MessageCreateRequest("유저2이 채널1에 작성한 메세지2", user2id, channel1id, null));
		messageService.create(new MessageCreateRequest("유저3이 채널1에 작성한 메세지3", user3id, channel1id, null));

		for(Message message : messageService.readAllByChannelId(channel1id)){
			System.out.println(message.getContent());
		}

		//#################################################
		System.out.println("\n===메세지 작성한 유저 삭제 테스트===\n");
		userService.delete(user3id);
		System.out.println("유저3 삭제 완료");
		userService.readAll().forEach(System.out::println);

		for(Message message : messageService.readAllByChannelId(channel1id)){
			System.out.println(message.getContent());
		}

		//#################################################
		System.out.println("\n===채널 어드민 유저 삭제 테스트===\n");
		System.out.println("\n유저1 삭제");
		userService.delete(user1id);

		System.out.println("\n유저2 속한 채널");
		userService.read(user2id).getJoinedChannelId().stream()
				.map(channelId -> channelService.read(channelId).getChannelName())
				.forEach(System.out::println);

		for(Message message : messageService.readAllByChannelId(channel1id)){
			System.out.println(message.getContent());
		}
	}
}
