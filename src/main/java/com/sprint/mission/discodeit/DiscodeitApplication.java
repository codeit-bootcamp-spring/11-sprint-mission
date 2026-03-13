package com.sprint.mission.discodeit;

//import com.sprint.mission.discodeit.control.ChannelControl;
//import com.sprint.mission.discodeit.control.MainControl;
//import com.sprint.mission.discodeit.control.MessageControl;
//import com.sprint.mission.discodeit.control.UserControl;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
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
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscodeitApplication.class, args);
	}

	@Bean
	public CommandLineRunner test(UserService userService) {
		return args -> {
			System.out.println("=== 고도화 서비스 테스트 시작 ===");

			try {
				// 유저 생성
				UserCreateRequest createRequest = new UserCreateRequest("테스터", "codeit@codeit.com", "password123");
				UserCreateRequest createRequest1 = new UserCreateRequest("코드잇", "sprint@codeit.com", "password12");
				UserDto newUser = userService.createUser(createRequest);
				UserDto newUser1 = userService.createUser(createRequest1);
				UUID targetId = newUser.id();
				System.out.println("생성된 유저 : " + newUser.name() + " (이메일 : " + newUser.email() + ")");
				System.out.println("생성된 유저 : " + newUser1.name() + " (이메일 : " + newUser1.email() + ")");

				// 유저 전체 조회
				System.out.println("현재 등록된 유저 수 : " + userService.allReadUser());

				// 유저 삭제
				userService.deleteUser(targetId, "password123");

				// 유저 전체 조회
				System.out.println("현재 등록된 유저 수 : " + userService.allReadUser());

				// 업데이트
				byte[] image = "image-content".getBytes();
				UserUpdateRequest updateRequest = new UserUpdateRequest("테스트_수정", "spring@spring.com", "password12", image);
				userService.updateUser(newUser1.id(), updateRequest);

				// 유저 조회
				UserDto updateUser = userService.readUser(newUser1.id());
				System.out.println("수정된 이름 : " + updateUser.name() + ", 수정된 이메일 : " + updateUser.email());
			} catch (Exception e) {
				System.out.println("테스트 중 오류 발생 : " + e.getMessage());
			}
		};
	}

}
