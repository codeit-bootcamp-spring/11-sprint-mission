package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageEditHistory;
import com.sprint.mission.discodeit.entity.User;
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

		System.out.println("=== 채팅 시스템 테스트 시작 ===\n");

		// 1. 유저 생성 테스트
		System.out.println("1. 유저 등록 테스트");
		User user1 = userService.createUser("Alice", "alice@example.com");
		User user2 = userService.createUser("Bob", "bob@example.com");
		User user3 = userService.createUser("Charlie", "charlie@example.com");
		System.out.println("생성된 유저: " + user1.getName() + ", " + user2.getName() + ", " + user3.getName());
		System.out.println();

		// 2. 유저 조회 테스트 (단건)
		System.out.println("2. 유저 조회 테스트 (단건)");
		User foundUser = userService.getUserById(user1.getId());
		System.out.println("조회된 유저: " + foundUser.getName() + " (" + foundUser.getEmail() + ")");
		System.out.println();

		// 3. 유저 조회 테스트 (다건)
		System.out.println("3. 유저 조회 테스트 (다건)");
		List<User> allUsers = userService.getAllUsers();
		System.out.println("전체 유저 수: " + allUsers.size());
		for (User u : allUsers) {
			System.out.println("  - " + u.getName() + " (" + u.getEmail() + ")");
		}
		System.out.println();

		// 4. 유저 수정 테스트
		System.out.println("4. 유저 수정 테스트");
		System.out.println("수정 전: " + user1.getName() + " (" + user1.getEmail() + ")");
		userService.updateUser(user1.getId(), "Alice Smith", "alice.smith@example.com");
		User updatedUser = userService.getUserById(user1.getId());
		System.out.println("수정 후: " + updatedUser.getName() + " (" + updatedUser.getEmail() + ")");
		System.out.println();

		// 5. 유저 삭제 테스트
		System.out.println("5. 유저 삭제 테스트");
		System.out.println("삭제 전 전체 유저 수: " + userService.getAllUsers().size());
		userService.deleteUser(user3.getId());
		System.out.println("삭제 후 전체 유저 수: " + userService.getAllUsers().size());
		User deletedUser = userService.getUserById(user3.getId());
		System.out.println("삭제된 유저 조회 결과: " + (deletedUser == null ? "null (삭제됨)" : deletedUser.getName()));
		System.out.println();

		// 6. 채널 등록 테스트
		System.out.println("6. 채널 등록 테스트");
		Channel channel1 = channelService.createChannel("일반 채팅방", 10, user1);
		Channel channel2 = channelService.createChannel("프로젝트 방", 5, user2);
		System.out.println("생성된 채널: " + channel1.getName() + ", " + channel2.getName());
		System.out.println();

		// 7. 채널 조회 테스트 (단건)
		System.out.println("7. 채널 조회 테스트 (단건)");
		Channel foundChannel = channelService.getChannelById(channel1.getId());
		System.out.println("조회된 채널: " + foundChannel.getName() + " (수용인원: " + foundChannel.getCapacity() + ")");
		System.out.println();

		// 8. 채널 조회 테스트 (다건)
		System.out.println("8. 채널 조회 테스트 (다건)");
		List<Channel> allChannels = channelService.getAllChannels();
		System.out.println("전체 채널 수: " + allChannels.size());
		for (Channel ch : allChannels) {
			System.out.println("  - " + ch.getName() + " (소유자: " + ch.getOwner().getName() + ")");
		}
		System.out.println();

		// 9. 채널 수정 테스트
		System.out.println("9. 채널 수정 테스트");
		System.out.println("수정 전: " + channel1.getName() + " (수용인원: " + channel1.getCapacity() + ")");
		channelService.updateChannel(channel1.getId(), "공지사항 방", 20);
		Channel updatedChannel = channelService.getChannelById(channel1.getId());
		System.out.println("수정 후: " + updatedChannel.getName() + " (수용인원: " + updatedChannel.getCapacity() + ")");
		System.out.println();

		// 10. 채널 삭제 테스트
		System.out.println("10. 채널 삭제 테스트");
		System.out.println("삭제 전 전체 채널 수: " + channelService.getAllChannels().size());
		channelService.deleteChannel(channel2.getId());
		System.out.println("삭제 후 전체 채널 수: " + channelService.getAllChannels().size());
		Channel deletedChannel = channelService.getChannelById(channel2.getId());
		System.out.println("삭제된 채널 조회 결과: " + (deletedChannel == null ? "null (삭제됨)" : deletedChannel.getName()));
		System.out.println();

		// 11. DM (Direct Message) 등록 테스트
		System.out.println("11. DM 등록 테스트");
		Message dm1 = messageService.sendDirectMessage(user1, user2, "안녕 Bob! 잘 지내?");
		System.out.println(user1.getName() + " -> " + user2.getName() + ": " + dm1.getContent());

		Message dm2 = messageService.sendDirectMessage(user2, user1, "안녕 Alice! 잘 지내고 있어!");
		System.out.println(user2.getName() + " -> " + user1.getName() + ": " + dm2.getContent());
		System.out.println();

		// 12. 채널 입장 테스트
		System.out.println("12. 채널 입장 테스트");
		boolean joined2 = channelService.joinChannel(channel1.getId(), user2);
		System.out.println(user2.getName() + " 입장 성공: " + joined2);

		List<User> participants = channelService.getChannelParticipants(channel1.getId());
		System.out.print("현재 참여자: ");
		for (User p : participants) {
			System.out.print(p.getName() + " ");
		}
		System.out.println("\n");

		// 13. 채널 메시지 전송 테스트
		System.out.println("13. 채널 메시지 전송 테스트");
		Message channelMsg1 = messageService.sendChannelMessage(user1, channel1, "환영합니다!");
		Message channelMsg2 = messageService.sendChannelMessage(user2, channel1, "안녕하세요~");

		List<Message> channelMessages = messageService.getMessagesInChannel(channel1);
		System.out.println("채널 메시지 목록:");
		for (Message msg : channelMessages) {
			System.out.println("  " + msg.getSender().getName() + ": " + msg.getContent());
		}
		System.out.println();

		// 14. 메시지 조회 테스트 (단건)
		System.out.println("14. 메시지 조회 테스트 (단건)");
		Message foundMessage = messageService.getMessageById(dm1.getId());
		System.out.println("조회된 메시지: " + foundMessage.getContent());
		System.out.println();

		// 15. 메시지 조회 테스트 (다건)
		System.out.println("15. 메시지 조회 테스트 (다건)");
		List<Message> allMessages = messageService.getAllMessages();
		System.out.println("전체 메시지 수: " + allMessages.size());
		System.out.println();

		// 16. 메시지 수정 테스트
		System.out.println("16. 메시지 수정 테스트");
		System.out.println("수정 전: " + dm1.getContent());
		messageService.updateMessage(dm1.getId(), "안녕 Bob! 오늘 날씨가 좋네!");
		Message updatedMessage = messageService.getMessageById(dm1.getId());
		System.out.println("수정 후: " + updatedMessage.getContent());

		List<MessageEditHistory> history = messageService.getMessageEditHistory(dm1.getId());
		System.out.println("수정 내역 개수: " + history.size());
		if (!history.isEmpty()) {
			System.out.println("이전 내용: " + history.get(0).getPreviousContent());
		}
		System.out.println();

		// 17. 메시지 삭제 테스트
		System.out.println("17. 메시지 삭제 테스트");
		System.out.println("삭제 전 isDeleted: " + messageService.isMessageDeleted(channelMsg2.getId()));
		messageService.deleteMessage(channelMsg2.getId());
		System.out.println("삭제 후 isDeleted: " + messageService.isMessageDeleted(channelMsg2.getId()));
		Message deletedMessage = messageService.getMessageById(channelMsg2.getId());
		System.out.println("삭제된 메시지 조회 결과: " + (deletedMessage.isDeleted() ? "삭제됨" : "존재함"));
		System.out.println();

		// 18. 유저 강퇴 테스트
		System.out.println("18. 유저 강퇴 테스트");
		List<User> beforeKick = channelService.getChannelParticipants(channel1.getId());
		System.out.print("강퇴 전 참여자: ");
		for (User p : beforeKick) System.out.print(p.getName() + " ");
		System.out.println();

		boolean kicked = channelService.kickUser(channel1.getId(), user1, user2);
		System.out.println(user1.getName() + "(소유자)가 " + user2.getName() + " 강퇴 결과: " + (kicked ? "성공" : "실패"));

		List<User> afterKick = channelService.getChannelParticipants(channel1.getId());
		System.out.print("강퇴 후 참여자: ");
		for (User p : afterKick) System.out.print(p.getName() + " ");
		System.out.println();

		System.out.println("=== 채팅 시스템 테스트 완료 ===");
	}

}
