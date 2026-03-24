package com.sprint.mission.dicordeit;

import com.sprint.mission.dicordeit.service.ChannelService;
import com.sprint.mission.dicordeit.service.MessageService;
import com.sprint.mission.dicordeit.service.UserService;

import com.sprint.mission.dicordeit.service.file.FileUserService;
import com.sprint.mission.dicordeit.service.file.FileChannelService;
import com.sprint.mission.dicordeit.service.file.FileMessageService;

import com.sprint.mission.dicordeit.entity.Channel;
import com.sprint.mission.dicordeit.entity.Message;
import com.sprint.mission.dicordeit.entity.User;

import java.util.List;
import java.util.UUID;

public class JavaApplication {

    public static void main(String[] args) {
        UserService newUserService = new FileUserService();
        MessageService newMessageService = new FileMessageService();
        ChannelService newChannelService = new FileChannelService();


        User createdUser = newUserService.createUser("testUser", "1234", "codeit@codeit.com");
        UUID tagetId = createdUser.getId();
        System.out.println("등록 성공: " + createdUser.getName());

        // newUserService를 사용해 전체 유저 목록을 가져옵니다.
        List<User> allUsers = newUserService.readAll();

        System.out.println("현재 총 유저 수: " + allUsers.size() + "명");

        // 아까 저장해둔 tagetId를 건네주어 그 유저만 정확히 찾아옵니다.
        User foundUser = newUserService.readUser(tagetId);

        // 찾아온 유저의 이름을 출력해 봅니다.
        System.out.println("단건 조회 완료, 찾은 이름: " + foundUser.getUsername());

        // [4단계] 수정
        // 아까 빼둔 tagetId와 새로운 비밀번호, 새로운 이메일을 전달해서 정보를 덮어씁니다.
        // 힌트: 아까 인터페이스 만들 때 메서드 이름을 upDate 라고 지으셨던 걸로 기억합니다.
        newUserService.update(tagetId, "newPassword!", "newEmail@codeit.com");

        System.out.println("=== 정보 수정 완료 ===");

        // [5단계] 수정된 데이터 조회
        // 정말로 바뀌었는지 확인하기 위해, tagetId로 다시 한 명만 찾아옵니다. (아까 3단계랑 똑같습니다)
        User updatedUser = newUserService.readUser(tagetId);

        // 찾아온 유저의 '이메일'을 꺼내서 출력해 봅니다.
        // 힌트: User 클래스에 있는 이메일 꺼내오는 메서드 (get뭐시기...)
        System.out.println("수정된 이메일 확인: " + updatedUser.getEmail());

        // [6단계] 삭제
        // 아까부터 계속 쓰고 있는 tagetId를 줘서 해당 유저를 리스트에서 아예 지워버립니다.
        newUserService.delete(tagetId);

        System.out.println("=== 데이터 삭제 완료 ===");

        // [7단계] 삭제 확인
        // 정말로 지워졌는지 확인하기 위해, tagetId로 다시 한번 찾아와 봅니다.
        User deletedUser = newUserService.readUser(tagetId);

        // 지워졌다면 아무것도 못 찾았을 테니 null 이 나와야 정상입니다.
        System.out.println("삭제 후 조회 결과 (null이 나와야 정상): " + deletedUser);




    }
}
