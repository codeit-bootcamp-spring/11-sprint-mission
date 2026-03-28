package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.request.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.channel.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.request.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.message.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.request.user.CreateUserRequest;
import com.sprint.mission.discodeit.dto.request.user.LoginRequest;
import com.sprint.mission.discodeit.dto.request.user.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context =
				SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		AuthService authService = context.getBean(AuthService.class);
//
//		System.out.println("=== 채팅 시스템 테스트 시작 ===\n");
//
//		// 1. 유저 생성 테스트
//		System.out.println("1. 유저 등록 테스트");
//		UserResponse user1 = userService.createUser(new CreateUserRequest("Alice", "alice@example.com", "password1", null), null);
//		UserResponse user2 = userService.createUser(new CreateUserRequest("Bob", "bob@example.com", "password2", null), null);
//		UserResponse user3 = userService.createUser(new CreateUserRequest("Charlie", "charlie@example.com", "password3", null), null);
//		System.out.println("생성된 유저: " + user1.getUsername() + ", " + user2.getUsername() + ", " + user3.getUsername());
//		System.out.println();
//
//		// 2. 유저 조회 테스트 (단건)
//		System.out.println("2. 유저 조회 테스트 (단건)");
//		UserResponse foundUser = userService.getUserById(user1.getUserId());
//		System.out.println("조회된 유저: " + foundUser.getUsername() + " (" + foundUser.getEmail() + ")");
//		System.out.println();
//
//		// 3. 유저 조회 테스트 (다건)
//		System.out.println("3. 유저 조회 테스트 (다건)");
//		List<UserResponse> allUsers = userService.getAllUsers();
//		System.out.println("전체 유저 수: " + allUsers.size());
//		for (UserResponse u : allUsers) {
//			System.out.println("  - " + u.getUsername() + " (" + u.getEmail() + ") 온라인: " + u.isOnline());
//		}
//		System.out.println();
//
//		// 4. 유저 수정 테스트
//		System.out.println("4. 유저 수정 테스트");
//		System.out.println("수정 전: " + user1.getUsername() + " (" + user1.getEmail() + ")");
//		UserResponse updatedUser = userService.updateUser(user1.getUserId(), new UpdateUserRequest("Alice Smith", "alice.smith@example.com", "newpassword", null), null);
//		System.out.println("수정 후: " + updatedUser.getUsername() + " (" + updatedUser.getEmail() + ")");
//		System.out.println();
//
//		// 5. 유저 삭제 테스트
//		System.out.println("5. 유저 삭제 테스트");
//		System.out.println("삭제 전 전체 유저 수: " + userService.getAllUsers().size());
//		userService.deleteUser(user3.getUserId());
//		System.out.println("삭제 후 전체 유저 수: " + userService.getAllUsers().size());
//		System.out.println();
//
//		// 6. 로그인 테스트
//		System.out.println("6. 로그인 테스트");
//		UserResponse loggedIn = authService.login(new LoginRequest("Alice Smith", "newpassword"));
//		System.out.println("로그인 성공: " + loggedIn.getUsername());
//		try {
//			authService.login(new LoginRequest("Alice", "wrongpassword"));
//		} catch (IllegalArgumentException e) {
//			System.out.println("로그인 실패 (예외 발생): " + e.getMessage());
//		}
//		System.out.println();
//
//		// 7. PUBLIC 채널 생성 테스트
//		System.out.println("7. PUBLIC 채널 생성 테스트");
//		ChannelResponse channel1 = channelService.createPublicChannel(new CreatePublicChannelRequest("일반 채팅방", "누구나 참여 가능한 채널"));
//		ChannelResponse channel2 = channelService.createPublicChannel(new CreatePublicChannelRequest("공지사항", "공지사항 채널"));
//		System.out.println("생성된 채널: " + channel1.getName() + ", " + channel2.getName());
//		System.out.println();
//
//		// 8. PRIVATE 채널 생성 테스트
//		System.out.println("8. PRIVATE 채널 생성 테스트");
//		ChannelResponse privateChannel = channelService.createPrivateChannel(
//				new CreatePrivateChannelRequest(List.of(user1.getUserId(), user2.getUserId()))
//		);
//		System.out.println("PRIVATE 채널 생성 완료, 참여자 수: " + privateChannel.getParticipantIds().size());
//		System.out.println();
//
//		// 9. 채널 조회 테스트
//		System.out.println("9. 채널 조회 테스트 (단건)");
//		ChannelResponse foundChannel = channelService.getChannelById(channel1.getId());
//		System.out.println("조회된 채널: " + foundChannel.getName() + " (" + foundChannel.getDescription() + ")");
//		System.out.println();
//
//		// 10. 채널 목록 조회 테스트 (특정 유저 기준)
//		System.out.println("10. 채널 목록 조회 테스트 (user1 기준)");
//		List<ChannelResponse> userChannels = channelService.findAllByUserId(user1.getUserId());
//		System.out.println("user1이 볼 수 있는 채널 수: " + userChannels.size());
//		for (ChannelResponse ch : userChannels) {
//			System.out.println("  - " + (ch.getName() != null ? ch.getName() : "PRIVATE 채널"));
//		}
//		System.out.println();
//
//		// 11. 채널 수정 테스트
//		System.out.println("11. 채널 수정 테스트");
//		System.out.println("수정 전: " + channel1.getName());
//		ChannelResponse updatedChannel = channelService.updateChannel(channel1.getId(), new UpdateChannelRequest("자유 채팅방", "자유롭게 대화하는 채널"));
//		System.out.println("수정 후: " + updatedChannel.getName());
//		System.out.println();
//
//		// 12. 메시지 전송 테스트
//		System.out.println("12. 메시지 전송 테스트");
//		Message msg1 = messageService.createMessage(new CreateMessageRequest("안녕하세요!", channel1.getId(), user1.getUserId(), null));
//		Message msg2 = messageService.createMessage(new CreateMessageRequest("반갑습니다~", channel1.getId(), user2.getUserId(), null));
//		System.out.println("메시지 전송 완료");
//		System.out.println();
//
//		// 13. 채널 메시지 조회 테스트
//		System.out.println("13. 채널 메시지 조회 테스트");
//		List<Message> channelMessages = messageService.findAllByChannelId(channel1.getId());
//		System.out.println("채널 메시지 수: " + channelMessages.size());
//		for (Message msg : channelMessages) {
//			System.out.println("  - " + msg.getContent());
//		}
//		System.out.println();
//
//		// 14. 메시지 수정 테스트
//		System.out.println("14. 메시지 수정 테스트");
//		System.out.println("수정 전: " + msg1.getContent());
//		messageService.updateMessage(msg1.getId(), new UpdateMessageRequest("안녕하세요! 반갑습니다!"));
//		Message updatedMsg = messageService.getMessageById(msg1.getId());
//		System.out.println("수정 후: " + updatedMsg.getContent());
//		System.out.println();
//
//		// 15. 메시지 삭제 테스트
//		System.out.println("15. 메시지 삭제 테스트");
//		System.out.println("삭제 전 isDeleted: " + messageService.isMessageDeleted(msg2.getId()));
//		messageService.deleteMessage(msg2.getId());
//		System.out.println("삭제 후 isDeleted: " + messageService.isMessageDeleted(msg2.getId()));
//		System.out.println();
//
//		// 16. 채널 삭제 테스트
//		System.out.println("16. 채널 삭제 테스트");
//		System.out.println("삭제 전 채널 수: " + channelService.findAllByUserId(user1.getUserId()).size());
//		channelService.deleteChannel(channel2.getId());
//		System.out.println("삭제 후 채널 수: " + channelService.findAllByUserId(user1.getUserId()).size());
//		System.out.println();
//
//		System.out.println("=== 채팅 시스템 테스트 완료 ===");
	}
}