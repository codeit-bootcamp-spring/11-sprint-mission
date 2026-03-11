package com.sprint.mission.discodeit.run;



import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FILEChannelRepository;
import com.sprint.mission.discodeit.repository.file.FILEMessageRepository;
import com.sprint.mission.discodeit.repository.file.FILEUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.file.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static com.sprint.mission.discodeit.entity.User.Status.*;

public class javaApplication {

    public static void main(String[] args) {

        UserService userService;
        ChannelService channelService;
        MessageService messageService;

        UserRepository userRepository;
        ChannelRepository channelRepository;
        MessageRepository messageRepository;

//        userRepository = new JCFUserRepository();
//        channelRepository = new JCFChannelRepository();
//        messageRepository = new JCFMessageRepository();


        userRepository = new FILEUserRepository();
        channelRepository = new FILEChannelRepository();
        messageRepository = new FILEMessageRepository();

        userService = new BasicUserService(userRepository, channelRepository, messageRepository);
        channelService = new BasicChannelService(channelRepository,userRepository,messageRepository);
        messageService = new BasicMessageService(messageRepository,channelRepository,userRepository);






        System.out.println("---------------------------------------------------");
        System.out.println("1. 유저 CRUD 테스트");
        System.out.println("---------------------------------------------------");
        System.out.println("1-1. Create User ");

        //유저 5명 생성
        userService.createUser("김하나", "hanakim1111", "khn0101");
        userService.createUser("이두리", "durilee2222", "ldr0202");
        userService.createUser("박사미", "samipark3333", "psm0303");
        userService.createUser("최사포", "sapochoi4444", "csp0404");
        userService.createUser("정오영", "oyeongjeong5555", "joy0505");
        System.out.println("\n");

        System.out.println("1-2. Read User");

        // 개인 출력
        userService.readUser("ldr0202");
        userService.readUser("csp0404");

        //전체 출력
        System.out.println("\n모든 유저 출력\n");
        userService.readAllUser();
        System.out.println("\n");

        System.out.println("1-3. Update User");

        //닉네임 변경
        userService.updateNickname("khn0101","hanakim1111","김한나");

        // 비밀번호 변경(틀린 비밀번호)
        userService.updatePassword("khn0101","wrongpassword","wrongpassword2");

        //비밀번호 변경(정상적)
        userService.updatePassword("khn0101","hanakim1111","hannakim1111");
        //상태 변경(비활성화)
        userService.updateStatus("khn0101", "hannakim1111", INACTIVE);

        //전체 출력
        userService.readUser("khn0101");
        System.out.println("\n");


        //2번째 유저 삭제 후 유저 목록 출력
        System.out.println("1-4. Delete User");
        userService.deleteUser("ldr0202","durilee2222");
        userService.readAllUser();

        System.out.println("\n");




        System.out.println("---------------------------------------------------");
        System.out.println("2. 채널 CRUD 테스트");
        System.out.println("---------------------------------------------------");



        System.out.println("2-1. Create Channel");
        //채널 3개 생성
        channelService.createChannel("통새우와퍼애호가들", "khn0101","shrimplove0001");
        channelService.createChannel("빅맥애호가들", "joy0505","bigmaclove0001");
        channelService.createChannel("불고기버거애호가들", "psm0303","bulgogilove0001");

        System.out.println("\n");


        System.out.println("2-2. Read Channel");

        //채널 하나 출력
        channelService.readChannel("shrimplove0001");

        //채널 전체 출력
        System.out.println("\n모든 채널 출력\n");

        channelService.readAllChannel();

        System.out.println("\n");

        System.out.println("2-3. Update Channel");

        System.out.println("2-3-1. add/remove Member");

        //채널 멤버 추가
        channelService.addMember("shrimplove0001","csp0404");
        channelService.addMember("shrimplove0001","joy0505");

        channelService.readChannel("shrimplove0001");

        System.out.println("\n//////////////////\n");
        //채널 멤버 삭제 & 방장 삭제시 자동 권한 이양
        channelService.removeMember("shrimplove0001","khn0101");
        channelService.readChannel("shrimplove0001");

        System.out.println("\n");

        //채널 이름 변경
        System.out.println("2-3-2. Update Channel Name");
        channelService.updateChannelName("shrimplove0001","더블통새우와퍼애호가들");
        channelService.readChannel("shrimplove0001");
        System.out.println("\n");

        //채널장 변경
        System.out.println("2-3-3. Update Channel Owner");
        channelService.updateChannelOwner("shrimplove0001","joy0505");
        channelService.readChannel("shrimplove0001");
        System.out.println("\n");

        //채널 삭제
        System.out.println("2-4. Delete Channel");
        channelService.deleteChannel("shrimplove0001");
        channelService.readAllChannel();

        System.out.println("\n");

        System.out.println("---------------------------------------------------");
        System.out.println("3. 메세지 CRUD 테스트");
        System.out.println("---------------------------------------------------");



        System.out.println("3-1. Create Message");
        //채널 멤버 2명 만들기
        channelService.addMember("bigmaclove0001","csp0404");


        messageService.sendMessage("csp0404","bigmaclove0001", "나는 빅맥이 너무 좋아");
        messageService.sendMessage("joy0505","bigmaclove0001", "참깨빵 위에");
        messageService.sendMessage("joy0505", "bigmaclove0001", "순쇠고기 패티 두장");
        messageService.sendMessage("joy0505", "bigmaclove0001", "특별한 소스");
        messageService.sendMessage("joy0505", "bigmaclove0001", "양상추");
        messageService.sendMessage("joy0505", "bigmaclove0001", "치즈");
        messageService.sendMessage("joy0505", "bigmaclove0001", "피클");
        messageService.sendMessage("joy0505", "bigmaclove0001", "양파까지");

        //모두 출력
        messageService.readAllMessage();
        System.out.println("\n");




        System.out.println("3-2. Read Message");

        // 메시지 아이디 받아와서 출력
        String mID1 = messageService.sendMessage("joy0505", "bigmaclove0001", "빠빠빠라빠");

        messageService.readMessage(mID1);

        //전체 출력
        System.out.println("\n모든 메시지 출력!\n");
        messageService.readAllMessage();
        System.out.println("\n");


        System.out.println("3-3. Update Message");

        //메시지 내용 업데이트
        messageService.updateMessage(mID1,"뚜루뚜빠라빠라");
        messageService.readMessage(mID1);

        System.out.println("\n");


        //메시지 삭제
        System.out.println("3-4. Delete Message");
        messageService.deleteMessage(mID1);
        messageService.readAllMessage();

        userService.deleteUser("joy0505","oyeongjeong5555");
        channelService.readChannel("bigmaclove0001");





        //파일 정리
        try (var stream = Files.list(Path.of("src/main/resources/users/"))) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);



        } catch (IOException e) {
            throw new RuntimeException("삭제 중 오류 발생", e);

        }

        try (var stream = Files.list(Path.of("src/main/resources/Channels/"))) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);



        } catch (IOException e) {
            throw new RuntimeException("삭제 중 오류 발생", e);

        }
        try (var stream = Files.list(Path.of("src/main/resources/Messages/"))) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);



        } catch (IOException e) {
            throw new RuntimeException("삭제 중 오류 발생", e);

        }
    }

}
