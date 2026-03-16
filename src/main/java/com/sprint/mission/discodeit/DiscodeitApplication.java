package com.sprint.mission.discodeit;

//import com.sprint.mission.discodeit.control.ChannelControl;
//import com.sprint.mission.discodeit.control.MainControl;
//import com.sprint.mission.discodeit.control.MessageControl;
//import com.sprint.mission.discodeit.control.UserControl;
import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.menu.MainMenu;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscodeitApplication.class, args);
	}

	@Bean
	public CommandLineRunner test(
			UserService userService,
			ChannelService channelService,
			MessageService messageService, ReadStatusService readStatusService) {
		return args -> {
			System.out.println("=== 고도화 서비스 테스트 시작 ===");

			try {
				// 유저 생성
				UserCreateRequest createRequest = new UserCreateRequest("테스터", "codeit@codeit.com", "password123");
				UserCreateRequest createRequest1 = new UserCreateRequest("코드잇", "sprint@codeit.com", "password12");
				UserDto newUser = userService.createUser(createRequest);
				UserDto newUser1 = userService.createUser(createRequest1);
				UUID targetId = newUser1.id();
				System.out.println("생성된 유저 : " + newUser.name() + " (이메일 : " + newUser.email() + ")");
				System.out.println("생성된 유저 : " + newUser1.name() + " (이메일 : " + newUser1.email() + ")");

				// 유저 전체 조회
				System.out.println("현재 등록된 유저 수 : " + userService.allReadUser());

				// 유저 삭제
				userService.deleteUser(targetId, "password12");

				// 유저 전체 조회
				System.out.println("현재 등록된 유저 수 : " + userService.allReadUser());

				// 업데이트
				byte[] image = "image-content".getBytes();
				UserUpdateRequest updateRequest = new UserUpdateRequest("테스트_수정", "spring@spring.com", "password12", image);
				userService.updateUser(newUser.id(), updateRequest);

				// 유저 조회
				UserDto updateUser = userService.readUser(newUser.id());
				System.out.println("수정된 이름 : " + updateUser.name() + ", 수정된 이메일 : " + updateUser.email());

				// PUBLIC 채널 생성
				System.out.println("\n--- 채널 생성 테스트 ---");
				PublicChannelRequest publicChannelRequest = new PublicChannelRequest("스프링", "스프링 클래스", ChannelType.PUBLIC, newUser.id());
				Channel channel = channelService.createPublicChannel(publicChannelRequest);
				System.out.println("✅ 공개 채널 생성 완료 : " + channel.getName());

				// 메시지 전송
				System.out.println("\n--- 메시지 생성 테스트 ---");
				CreateMessageRequest messageRequest = new CreateMessageRequest(channel.getId(), newUser.id(), "안녕하세요!", List.of());
				MessageResponseDto sendMessage = messageService.createMessage(messageRequest);
				System.out.println("✅ 메시지 전송 완료 : " + sendMessage.message());

				// ReadStatus 생성
				System.out.println("\n--- ReadStatus 생성 테스트 ---");
				ReadStatusCreateDto createDto = new ReadStatusCreateDto(newUser.id(), channel.getId());
				readStatusService.createReadStatus(createDto);

				// ReadStatus 조회 및 업데이트
				System.out.println("\n--- ReadStatus 조회 및 업데이트 ---");
				List<ReadStatus> statusList = readStatusService.findAllByUserId(newUser.id());
				ReadStatus myStatus = statusList.get(0);
				System.out.println("✅ 기존 ReadStatus : " + myStatus.getLastReadAt());

				Instant now = Instant.now();
				ReadStatusUpdateDto updateDto = new ReadStatusUpdateDto(myStatus.getId(), now);
				ReadStatus updatedStatus = readStatusService.updateStatus(updateDto);
				System.out.println("✅ 업데이트 된 ReadStatus : " + updatedStatus.getLastReadAt() + " 유저 : " + updatedStatus.getUserId());

				// 채널 상세 조회
				System.out.println("\n--- 채널 상세 조회 테스트 ---");
				ChannelResponse channelDetail = channelService.readChannel(channel.getId());
				System.out.println("✅ 조회 채널명 : " + channelDetail.name());
				System.out.println("✅ 조회 채널 타입 : " + channelDetail.type());
				System.out.println("✅ 마지막 메시지 시간 : " + channelDetail.lastMessageAt());

				// 유저가 참여 중인 채널목록 조회
				System.out.println("\n--- 유저별 채널 목록 조회 테스트 ---");
				List<ChannelResponse> myChannels = channelService.findAllByUserId(newUser.id());
				System.out.println(newUser.name() + "님의 채널 수 : " + myChannels.size());
				myChannels.forEach(c -> System.out.println("채널 : " + c.name() + ", 마지막 활동 : " + c.lastMessageAt()));

				// 채널 정보 업데이트
				System.out.println("\n--- 채널 정보 업데이트 테스트 ---");
				ChannelUpdate update = new ChannelUpdate(channel.getId(), "스프린트", "코드잇 스프린트");
				channelService.updateChannel(update);
				System.out.println("✅ 채널 업데이트 완료");
				channelService.readChannel(channel.getId());

				// ReadStatus 삭제
				System.out.println("\n--- ReadStatus 삭제 테스트 ---");
				readStatusService.deleteReadStatus(myStatus.getId());
				System.out.println("✅ 읽음 상태 데이터 삭제");

				// 메시지 삭제 및 채널 삭제
				System.out.println("\n--- 메시지 및 채널 삭제 테스트 ---");
				channelService.deleteChannel(channel.getId());
				System.out.println("❌ 채널 삭제 완료");
			} catch (Exception e) {
				System.out.println("❌ 테스트 중 오류 발생 : " + e.getMessage());
				e.printStackTrace();
			}
		};
	}

}
