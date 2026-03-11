package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Entity;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class JCFUserService implements UserService {

    final Map<String, User> data;

    public JCFUserService(){

        data = new HashMap<>();

    }

    @Override
    public void createUser(String nickname, String password, String userId) {

        if(isExistUser(userId)) {
            System.out.println("이미 존재하는 유저의 id입니다");
            return;



        }
        User user = new User(userId, password, nickname);
        data.put(userId, user);

        System.out.println( nickname + "님 생성 완료!");


    }

    @Override
    public void readUser(String userId) {

        if(!isExistUser(userId)){

            System.out.println("해당 id를 사용하는 유저가 없습니다.");
            return;

        }

        User user = data.get(userId);
        System.out.println(user);


    }

    @Override
    public void readAllUser() {

        data.values().stream()
                .sorted(Entity::compareTo)
                .forEach(System.out::println);



    }

    @Override
    public void updateNickname(String userId, String password,String nickname) {


        if(!isExistUser(userId)){
            System.out.println("존재하지 않는 유저 아이디 입니다.");
            return;
        }
        User user = data.get(userId);
        user.updateNickname(nickname,password);



    }

    @Override
    public void updatePassword(String userId, String oldPassword, String newPassword) {

        if(!isExistUser(userId)){
            System.out.println("존재하지 않는 유저 아이디입니다.");
            return;

        }


        User user = data.get(userId);
        if(!user.updatePassword(oldPassword, newPassword)){

            System.out.println("비밀번호가 잘못 입력되었습니다.");
            return;
        }
        System.out.println(user.getNickname() + "님 비밀번호 변경 완료!");

    }

    @Override
    public void updateStatus(String userId, String password, User.Status status) {

        if(!isExistUser(userId)){

            System.out.println("존재하지 않는 유저 아이디입니다.");
            return;

        }
        User user = data.get(userId);
        user.updateStatus(status,password);


    }

    @Override
    public void deleteUser(String userId, String password) {

        if(!isExistUser(userId)){

            System.out.println("존재하지 않는 유저 아이디입니다.");
            return;

        }

        User user = data.get(userId);
        if(user.checkSamePassword(password)){
            data.remove(userId);
            System.out.println(user.getNickname() + "님 계정 삭제 완료!");
            return;
        }
        System.out.println("비밀번호가 같지 않습니다.");

    }

    //중복 체크
    public boolean isExistUser(String userId){

        return data.get(userId) != null;


    }





}
