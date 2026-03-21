package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequestDto;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponseDto;
import com.sprint.mission.discodeit.dto.channel.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.login.LoginRequestDto;
import com.sprint.mission.discodeit.dto.login.LoginResponseDto;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequestDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequestDto;
import com.sprint.mission.discodeit.dto.user.UserResponseDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponseDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequestDto;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

	static UserResponseDto setupUser(UserService userService) {
		return userService.create(
				new UserCreateRequestDto("woody", "woody@codeit.com", "woody1234", null));
	}

	static ChannelResponseDto setupChannel(ChannelService channelService) {
		return channelService.createPublicChannel(
				new PublicChannelCreateRequestDto("공지", "공지 채널입니다."));
	}

	static BinaryContentResponseDto binaryContentTest(BinaryContentService binaryContentService) {
		System.out.println("\n=== BinaryContent 테스트 ===");

		BinaryContentResponseDto binaryContent = binaryContentService.create(
				new BinaryContentCreateRequestDto(
						"hello.txt", "첨부파일 내용입니다.".getBytes(StandardCharsets.UTF_8)));

		System.out.println("BinaryContent 생성: " + binaryContent.id());
		System.out.println("파일명: " + binaryContent.fileName());
		System.out.println("데이터 길이: " + binaryContent.data().length);

		BinaryContentResponseDto found = binaryContentService.find(binaryContent.id());
		System.out.println("BinaryContent 조회 성공: " + found.id());

		List<BinaryContentResponseDto> foundList =
				binaryContentService.findAllByIdIn(List.of(binaryContent.id()));
		System.out.println("BinaryContent 다건 조회 성공: " + foundList.size() + "개");

		return binaryContent;
	}

	static Message messageCreateTest(MessageService messageService, UUID channelId, UUID authorId, UUID binaryContentId) {
		System.out.println("\n=== Message 테스트 ===");

		Message message = messageService.create(
				new MessageCreateRequestDto(
						"안녕하세요.",
						channelId,
						authorId,
						new ArrayList<>(List.of(binaryContentId))
				)
		);

		System.out.println("메시지 생성: " + message.getId());
		System.out.println("메시지 첨부파일 개수: " + message.getAttachmentIds().size());

		return message;
	}

	static void authLoginTest(AuthService authService) {

		System.out.println("\n=== Auth 테스트 ===");

		LoginResponseDto response = authService.login(
				new LoginRequestDto("woody", "woody1234")
		);

		System.out.println("로그인 성공: " + response.id());
		System.out.println("이름: " + response.name());
		System.out.println("이메일: " + response.email());
	}

	static UserStatusResponseDto userStatusTest(
			UserStatusService userStatusService, UUID userId, UUID userStatusId) {

		System.out.println("\n=== UserStatus 테스트 ===");

		UserStatusResponseDto found = userStatusService.find(userStatusId);
		System.out.println("UserStatus 조회 성공: " + found.id());

		userStatusService.update(new UserStatusUpdateRequestDto(found.id()));
		UserStatusResponseDto updated = userStatusService.find(found.id());
		System.out.println("UserStatus update 후 updatedAt: " + updated.updatedAt());

		return found;
	}

	static ReadStatusResponseDto readStatusTest(
			ReadStatusService readStatusService, UUID userId, UUID channelId) {

		System.out.println("\n=== ReadStatus 테스트 ===");

		ReadStatusResponseDto created = readStatusService.create(
				new ReadStatusCreateRequestDto(userId, channelId)
		);
		System.out.println("ReadStatus 생성: " + created.id());

		ReadStatusResponseDto found = readStatusService.find(created.id());
		System.out.println("ReadStatus 조회 성공: " + found.id());

		List<ReadStatusResponseDto> userReadStatuses = readStatusService.findAllByUserId(userId);
		System.out.println("해당 유저의 ReadStatus 개수: " + userReadStatuses.size());

		readStatusService.update(new ReadStatusUpdateRequestDto(created.id()));
		ReadStatusResponseDto updated = readStatusService.find(created.id());
		System.out.println("ReadStatus update 후 updatedAt: " + updated.updatedAt());

		return created;
	}

	static void deleteTest(
			ReadStatusService readStatusService, UserStatusService userStatusService, BinaryContentService binaryContentService,
			UUID readStatusId, UUID userStatusId, UUID binaryContentId) {
		System.out.println("\n=== 삭제 테스트 ===");

		readStatusService.delete(readStatusId);
		System.out.println("ReadStatus 삭제 완료: " + readStatusId);

		userStatusService.delete(userStatusId);
		System.out.println("UserStatus 삭제 완료: " + userStatusId);

		binaryContentService.delete(binaryContentId);
		System.out.println("BinaryContent 삭제 완료: " + binaryContentId);
	}

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		AuthService authService = context.getBean(AuthService.class);
		UserStatusService userStatusService = context.getBean(UserStatusService.class);
		ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
		BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);

		UserResponseDto userResponseDto = setupUser(userService);
		ChannelResponseDto channelResponseDto = setupChannel(channelService);

		authLoginTest(authService);

		UserStatusResponseDto userStatusResponseDto = userStatusTest(userStatusService, userResponseDto.id(), userResponseDto.statusId());

		ReadStatusResponseDto readStatusResponseDto = readStatusTest(readStatusService, userResponseDto.id(), channelResponseDto.id());

		BinaryContentResponseDto binaryContentResponseDto = binaryContentTest(binaryContentService);

		messageCreateTest(messageService, channelResponseDto.id(), userResponseDto.id(), binaryContentResponseDto.id());

		deleteTest(readStatusService, userStatusService, binaryContentService,
				readStatusResponseDto.id(), userStatusResponseDto.id(), binaryContentResponseDto.id());

		System.out.println("\n=== 전체 테스트 완료 ===");
	}
}
