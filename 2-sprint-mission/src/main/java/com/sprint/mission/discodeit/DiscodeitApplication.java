package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@SpringBootApplication
public class DiscodeitApplication {

	// [BinaryContent & User] 셋업
	static UserDto.Response setupUser(UserService userService, BinaryContentService binaryContentService) {
		BinaryContentDto.Response profileImg = binaryContentService.create(
				new BinaryContentDto.CreateRequest("profile.png", 1024L, "image/png", new byte[]{1, 2, 3})
		);

		return userService.create(
				new UserDto.CreateRequest("woody", "pass123", "woody@codeit.com", "우디", "반가워요", profileImg.id())
		);
	}

	// [Channel] 셋업 (Public)
	static ChannelDto.Response setupPublicChannel(ChannelService channelService) {
		return channelService.createPublicChannel(
				new ChannelDto.CreatePublicRequest("공지사항", "전체 공지 채널")
		);
	}

	// [UserStatus] 테스트
	static void userStatusTest(UserStatusService userStatusService, UUID userId) {
		System.out.println("\n--- UserStatus 테스트 ---");
		userStatusService.updateByUserId(userId, new UserStatusDto.UpdateRequest(Instant.now()));
		System.out.println("유저 상태 업데이트 완료 (UserId: " + userId + ")");
	}

	// [ReadStatus] 테스트
	static void readStatusTest(ReadStatusService readStatusService, UUID userId, UUID channelId) {
		System.out.println("\n--- ReadStatus 테스트 ---");
		ReadStatusDto.Response readStatus = readStatusService.create(
				new ReadStatusDto.CreateRequest(userId, channelId)
		);
		System.out.println("ReadStatus 생성 완료: " + readStatus.id());
	}

	// [Message] 테스트 (첨부파일 포함)
	static void messageCreateTest(MessageService messageService, BinaryContentService binaryContentService, UUID authorId, UUID channelId) {
		System.out.println("\n--- Message 테스트 ---");
		BinaryContentDto.Response attachment = binaryContentService.create(
				new BinaryContentDto.CreateRequest("test.pdf", 500L, "application/pdf", new byte[]{10, 20})
		);

		MessageDto.Response message = messageService.create(
				new MessageDto.CreateRequest(authorId, channelId, "통합 테스트 메시지", List.of(attachment.id()))
		);
		System.out.println("메시지 생성 완료: " + message.content());
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		// 서비스 빈 주입
		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		UserStatusService userStatusService = context.getBean(UserStatusService.class);
		ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
		BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);

		System.out.println("\n==============================");
		System.out.println("DISCODEIT 통합 테스트 시작");
		System.out.println("==============================");

		// 초기 데이터 셋업
		UserDto.Response user = setupUser(userService, binaryContentService);
		ChannelDto.Response channel = setupPublicChannel(channelService);

		// 테스트 수행
		userStatusTest(userStatusService, user.id());
		readStatusTest(readStatusService, user.id(), channel.id());
		messageCreateTest(messageService, binaryContentService, user.id(), channel.id());

		System.out.println("\n==============================");
		System.out.println("전체 도메인 테스트 완료");
		System.out.println("==============================");
	}
}
