package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;

@SpringBootApplication
public class DiscodeitApplication {

	static User setupUser(UserService userService) {
		User user = userService.create(new UserCreateRequestDto("woody", "woody@codeit.com", "woody1234", null));
		return user;
	}

	static Channel setupChannel(ChannelService channelService) {
		Channel channel = channelService.createPublicChannel(new PublicChannelCreateRequestDto("공지", "공지 채널입니다."));
		return channel;
	}

	static void messageCreateTest(MessageService messageService, Channel channel, User author) {
		Message message = messageService.create(new MessageCreateRequestDto("안녕하세요.", channel.getId(), author.getId(), new ArrayList<>()) );
		System.out.println("메시지 생성: " + message.getId());
	}

	static void authLoginTest(AuthService authService) {
		LoginResponseDto response = authService.login(
				new LoginRequestDto("woody", "woody1234")
		);

		System.out.println("로그인 성공: " + response.id());
		System.out.println("이름: " + response.name());
		System.out.println("이메일: " + response.email());
	}

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		// Basic*Service 구현체를 초기화하세요.
		UserService userService = context.getBean(UserService.class);
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		AuthService authService = context.getBean(AuthService.class);

		// 셋업
		User user = setupUser(userService);
		Channel channel = setupChannel(channelService);

		// 테스트
		authLoginTest(authService);
		messageCreateTest(messageService, channel, user);
	}

}
